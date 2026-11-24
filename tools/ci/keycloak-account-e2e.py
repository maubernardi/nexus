"""Un utente cambia i propri dati dalla pagina «il mio account» di Keycloak? (US-116)

Accede come la pagina dell'account (client account-console, codice con PKCE), poi prova a cambiare con la API
dell'account, uno alla volta, nome, username ed email. Esce con 0 se il risultato è quello atteso:
  accettato  nome ed email cambiano (il realm di prima di US-116: la prova vede davvero il buco; lo username lì è già
             in sola lettura, perché «Edit username» è spento)
  rifiutato  i tre cambi sono rifiutati come attributi in sola lettura (dopo keycloak-user-profile.sh)
Solo dati finti: gira contro un Keycloak di prova.

Uso: python3 -I keycloak-account-e2e.py <indirizzo di Keycloak> <utente> <password> accettato|rifiutato
"""

import base64
import hashlib
import html
import json
import re
import secrets
import sys
import urllib.error
import urllib.parse
import urllib.request
from http.cookiejar import CookieJar, DefaultCookiePolicy


class LocalhostPolicy(DefaultCookiePolicy):
    """Keycloak marca i cookie della sessione come Secure: su http://localhost la prova li rimanda comunque."""

    def return_ok_secure(self, cookie, request):  # noqa: D102 (API di http.cookiejar)
        return True


def main(base: str, username: str, password: str, expected: str) -> None:
    account = f"{base}/realms/nexus/account/"

    class NoRedirect(urllib.request.HTTPRedirectHandler):
        """Il ritorno alla pagina dell'account con il codice non si segue: il codice serve alla prova."""

        def redirect_request(self, req, fp, code, msg, headers, newurl):  # noqa: N802 (API di urllib)
            return None if newurl.startswith(account) else super().redirect_request(req, fp, code, msg, headers, newurl)

    jar = CookieJar(policy=LocalhostPolicy())
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar), NoRedirect())
    verifier = secrets.token_urlsafe(48)
    challenge = base64.urlsafe_b64encode(hashlib.sha256(verifier.encode()).digest()).rstrip(b"=").decode()
    query = urllib.parse.urlencode({
        "client_id": "account-console", "redirect_uri": account, "response_type": "code", "scope": "openid",
        "state": secrets.token_hex(8), "code_challenge": challenge, "code_challenge_method": "S256",
    })
    with opener.open(f"{base}/realms/nexus/protocol/openid-connect/auth?{query}") as response:
        page = response.read().decode()
    match = re.search(r'<form[^>]*action="([^"]+)"', page)
    if not match:
        sys.exit("Nessun modulo nella pagina di accesso di Keycloak")
    try:
        opener.open(html.unescape(match.group(1)),
                    urllib.parse.urlencode({"username": username, "password": password}).encode())
        sys.exit("Accesso non riuscito: Keycloak non è tornato alla pagina dell'account")
    except urllib.error.HTTPError as error:
        location = error.headers.get("Location", "")
    params = urllib.parse.parse_qs(urllib.parse.urlparse(location).query)
    params.update(urllib.parse.parse_qs(urllib.parse.urlparse(location).fragment))
    if "code" not in params:
        sys.exit("Accesso non riuscito: nessun codice nel ritorno")
    token_request = urllib.parse.urlencode({
        "grant_type": "authorization_code", "client_id": "account-console", "code": params["code"][0],
        "redirect_uri": account, "code_verifier": verifier,
    }).encode()
    with urllib.request.urlopen(f"{base}/realms/nexus/protocol/openid-connect/token", token_request) as response:
        token = json.load(response)["access_token"]

    def call(method: str, data: dict | None = None) -> tuple[int, str]:
        request = urllib.request.Request(account, method=method, data=None if data is None else json.dumps(data).encode(),
                                         headers={"Authorization": f"Bearer {token}", "Accept": "application/json",
                                                  "Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(request) as response:
                return response.status, response.read().decode()
        except urllib.error.HTTPError as error:
            return error.code, error.read().decode()

    status, body = call("GET")
    if status != 200:
        sys.exit(f"Lettura dell'account: {status}")
    me = json.loads(body)
    failures = []
    for field, value in (("firstName", "Cambiato"), ("username", "cambiato.da.solo"),
                         ("email", "cambiata.da.sola@nexus.local")):
        status, body = call("POST", dict(me, **{field: value}))
        refused = status == 400 and "error-user-attribute-read-only" in body
        print(f"{field}: {status} {'rifiutato' if refused else 'accettato' if status == 204 else body[:120]}")
        if (expected == "accettato" and field != "username" and status != 204) or (
                expected == "rifiutato" and not refused):
            failures.append(field)
        if status == 204:
            status, body = call("GET")
            me = json.loads(body)
    if failures:
        sys.exit(f"Atteso «{expected}», diverso per: {', '.join(failures)}")
    print(f"OK: dalla pagina dell'account i cambi sono {expected}")


if __name__ == "__main__":
    if len(sys.argv) != 5 or sys.argv[4] not in ("accettato", "rifiutato"):
        sys.exit(__doc__)
    main(*sys.argv[1:])
