"""Prova dell'ambiente di collaudo sul PC del PO (EN-11), per la CI e per il PC.

Con il collaudo già avviato (infra/collaudo/collaudo.sh avvia ...) fa da utente, solo dall'esterno:
  1. accesso di tutor1 dalla pagina di Keycloak, come la PWA (codice con PKCE): /api/v1/me risponde con il ruolo TUTOR;
  2. email del backend: il cambio dell'indirizzo manda il codice, e arriva nella casella finta (Mailpit);
  3. email di Keycloak: «Password dimenticata?» per operatore.cc arriva nella casella finta.
Nessuna email esce: Mailpit le tiene tutte.

Uso: python3 -I collaudo-e2e.py <password del collaudo>
"""

import base64
import hashlib
import html
import json
import re
import secrets
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from http.cookiejar import CookieJar, DefaultCookiePolicy

APP = "http://localhost:8090"
AUTH = "http://localhost:8091"
MAIL = "http://localhost:8025"


class LocalhostPolicy(DefaultCookiePolicy):
    """Keycloak marca i cookie della sessione come Secure: su http://localhost la prova li rimanda comunque."""

    def return_ok_secure(self, cookie, request):  # noqa: D102 (API di http.cookiejar)
        return True


class NoRedirect(urllib.request.HTTPRedirectHandler):
    """Il ritorno alla PWA con il codice non si segue: il codice serve alla prova."""

    def redirect_request(self, req, fp, code, msg, headers, newurl):  # noqa: N802 (API di urllib)
        if newurl.startswith(APP):
            return None
        return super().redirect_request(req, fp, code, msg, headers, newurl)


def form(page: str) -> str:
    match = re.search(r'<form[^>]*action="([^"]+)"', page)
    if not match:
        sys.exit("Nessun modulo nella pagina di Keycloak:\n" + page[:2000])
    return html.unescape(match.group(1))


def call(method: str, url: str, token: str | None = None, data: dict | None = None) -> tuple[int, str]:
    headers = {"Accept": "application/json"}
    body = None
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if data is not None:
        headers["Content-Type"] = "application/json"
        body = json.dumps(data).encode()
    request = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request) as response:
            return response.status, response.read().decode()
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode()


def login(username: str, password: str) -> str:
    jar = CookieJar(policy=LocalhostPolicy())
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar), NoRedirect())
    verifier = secrets.token_urlsafe(48)
    challenge = base64.urlsafe_b64encode(hashlib.sha256(verifier.encode()).digest()).rstrip(b"=").decode()
    query = urllib.parse.urlencode({
        "client_id": "nexus-frontend", "redirect_uri": f"{APP}/", "response_type": "code", "scope": "openid",
        "state": secrets.token_hex(8), "code_challenge": challenge, "code_challenge_method": "S256",
    })
    with opener.open(f"{AUTH}/realms/nexus/protocol/openid-connect/auth?{query}") as response:
        page = response.read().decode()
    fields = urllib.parse.urlencode({"username": username, "password": password}).encode()
    try:
        opener.open(urllib.parse.urljoin(AUTH, form(page)), fields)
        sys.exit(f"Accesso di {username} non riuscito: Keycloak non rimanda a NEXUS")
    except urllib.error.HTTPError as error:
        location = error.headers.get("Location", "")
        if error.code != 302 or not location.startswith(f"{APP}/"):
            sys.exit(f"Accesso di {username} non riuscito: {error.code} {location}")
    code = urllib.parse.parse_qs(urllib.parse.urlsplit(location).query).get("code")
    if not code:
        sys.exit(f"Accesso di {username}: nessun codice nel ritorno a NEXUS ({location})")
    token = urllib.parse.urlencode({
        "grant_type": "authorization_code", "client_id": "nexus-frontend", "code": code[0],
        "redirect_uri": f"{APP}/", "code_verifier": verifier,
    }).encode()
    with urllib.request.urlopen(f"{AUTH}/realms/nexus/protocol/openid-connect/token", token) as response:
        return json.load(response)["access_token"]


def wait_for_mail(address: str, timeout: float = 60) -> dict:
    deadline = time.time() + timeout
    while time.time() < deadline:
        with urllib.request.urlopen(f"{MAIL}/api/v1/messages") as response:
            for message in json.load(response).get("messages", []):
                if any(to.get("Address") == address for to in message.get("To", [])):
                    return message
        time.sleep(1)
    sys.exit(f"Nessuna email per {address} nella casella del collaudo")


def main() -> None:
    password = sys.argv[1]

    token = login("tutor1", password)
    status, body = call("GET", f"{APP}/api/v1/me", token)
    if status != 200 or "TUTOR" not in body:
        sys.exit(f"/api/v1/me per tutor1: {status} {body[:500]}")
    print("1. Accesso di tutor1 dalla pagina di Keycloak: /api/v1/me con il ruolo TUTOR")

    new_address = "tutor1.collaudo@nexus.local"
    status, body = call("POST", f"{APP}/api/v1/me/email-change", token, {"newEmail": new_address})
    if status != 200:
        sys.exit(f"Cambio dell'indirizzo: {status} {body[:500]}")
    message = wait_for_mail(new_address)
    call("DELETE", f"{APP}/api/v1/me/email-change", token)
    print(f"2. Email del backend nella casella finta: «{message.get('Subject')}»")

    jar = CookieJar(policy=LocalhostPolicy())
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
    query = urllib.parse.urlencode({"client_id": "nexus-frontend", "redirect_uri": f"{APP}/", "response_type": "code",
                                    "scope": "openid", "code_challenge": "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
                                    "code_challenge_method": "S256"})
    with opener.open(f"{AUTH}/realms/nexus/protocol/openid-connect/auth?{query}") as response:
        page = response.read().decode()
    link = re.search(r'href="([^"]*login-actions/reset-credentials[^"]*)"', page)
    if not link:
        sys.exit("Manca «Password dimenticata?» nella pagina di accesso")
    with opener.open(urllib.parse.urljoin(AUTH, html.unescape(link.group(1)))) as response:
        page = response.read().decode()
    with opener.open(urllib.parse.urljoin(AUTH, form(page)),
                     urllib.parse.urlencode({"username": "operatore.cc"}).encode()):
        pass
    message = wait_for_mail("operatore.cc@nexus.local")
    print(f"3. Email di Keycloak nella casella finta: «{message.get('Subject')}»")
    print("Ambiente di collaudo: OK")


if __name__ == "__main__":
    main()
