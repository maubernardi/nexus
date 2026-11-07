"""Prova completa del recupero della password con un Keycloak vero (US-1308), per la CI.

Fa da server di posta (SMTP minimo, solo libreria standard) e da utente:
«Password dimenticata?» -> email con il collegamento -> nuova password -> email di sicurezza.
Verifica che arrivino esattamente due email, e che il cambio non ne mandi altre.

Uso: python3 -I keycloak-password-reset-e2e.py <url keycloak> <username> <email>
Keycloak deve inviare la posta a questo host sulla porta 2525.
"""

import email
import email.policy
import html
import re
import socketserver
import sys
import threading
import time
import urllib.parse
import urllib.request
from http.cookiejar import CookieJar, DefaultCookiePolicy

SMTP_PORT = 2525
NEW_PASSWORD = "Una-password-nuova-1308"
MESSAGES: list[email.message.EmailMessage] = []


class SmtpSink(socketserver.StreamRequestHandler):
    """SMTP quanto basta per Keycloak (EHLO, MAIL, RCPT, DATA, QUIT), senza TLS né autenticazione."""

    def reply(self, line: str) -> None:
        self.wfile.write((line + "\r\n").encode())

    def handle(self) -> None:
        self.reply("220 sink")
        while line := self.rfile.readline():
            command = line.decode(errors="replace").strip().upper()
            if command.startswith(("EHLO", "HELO")):
                self.reply("250 sink")
            elif command == "DATA":
                self.reply("354 fine con .")
                lines = []
                while (data := self.rfile.readline()) not in (b".\r\n", b".\n", b""):
                    lines.append(data[1:] if data.startswith(b"..") else data)
                MESSAGES.append(email.message_from_bytes(b"".join(lines), policy=email.policy.default))
                self.reply("250 ok")
            elif command == "QUIT":
                self.reply("221 ciao")
                return
            else:
                self.reply("250 ok")


def wait_for_messages(count: int, timeout: float = 30) -> None:
    deadline = time.time() + timeout
    while len(MESSAGES) < count and time.time() < deadline:
        time.sleep(0.5)
    if len(MESSAGES) < count:
        sys.exit(f"Attese {count} email, arrivate {len(MESSAGES)}")


def body(message: email.message.EmailMessage) -> str:
    part = message.get_body(preferencelist=("plain", "html"))
    return part.get_content() if part else ""


def form(page: str) -> str:
    match = re.search(r'<form[^>]*action="([^"]+)"', page)
    if not match:
        sys.exit("Nessun modulo nella pagina:\n" + page[:2000])
    return html.unescape(match.group(1))


def main() -> None:
    base, username, address = sys.argv[1:4]
    server = socketserver.ThreadingTCPServer(("0.0.0.0", SMTP_PORT), SmtpSink)
    server.daemon_threads = True
    threading.Thread(target=server.serve_forever, daemon=True).start()

    class LocalhostPolicy(DefaultCookiePolicy):
        """Keycloak marca i cookie della sessione come Secure: la prova su http://localhost deve rimandarli comunque,
        anche durante i redirect."""

        def return_ok_secure(self, cookie, request):  # noqa: D102 (API di http.cookiejar)
            return True

    class LoggedRedirects(urllib.request.HTTPRedirectHandler):
        def redirect_request(self, req, fp, code, msg, headers, newurl):  # noqa: N802 (API di urllib)
            target = urllib.parse.urlsplit(newurl)
            query = urllib.parse.parse_qs(target.query)
            detail = {k: v for k, v in query.items() if k in ("error", "error_description", "kc_action_status")}
            print(f"   {code} -> {target.netloc}{target.path} {detail or ''}")
            return super().redirect_request(req, fp, code, msg, headers, newurl)

    jar = CookieJar(policy=LocalhostPolicy())
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar), LoggedRedirects())

    def get(url: str) -> str:
        with opener.open(url) as response:
            return response.read().decode()

    def post(url: str, fields: dict[str, str]) -> str:
        with opener.open(url, urllib.parse.urlencode(fields).encode()) as response:
            return response.read().decode()

    auth = (f"{base}/realms/nexus/protocol/openid-connect/auth?client_id=nexus-frontend"
            "&redirect_uri=http%3A%2F%2Flocalhost%3A5173%2F&response_type=code&scope=openid"
            "&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256")
    login = get(auth)
    link = re.search(r'href="([^"]*login-actions/reset-credentials[^"]*)"', login)
    if not link:
        sys.exit("Manca «Password dimenticata?» nella pagina di accesso")
    reset_url = urllib.parse.urljoin(base, html.unescape(link.group(1)))

    # 1. «Password dimenticata?»: arriva l'email con il collegamento
    post(urllib.parse.urljoin(base, form(get(reset_url))), {"username": username})
    wait_for_messages(1)
    first = MESSAGES[0]
    if address not in first["To"]:
        sys.exit(f"Email del collegamento al destinatario sbagliato: {first['To']}")
    action = re.search(r'(https?://[^\s"<>]+login-actions/action-token[^\s"<>]+)', body(first))
    if not action:
        sys.exit("Nessun collegamento nell'email:\n" + body(first))
    print(f"1. Email del collegamento: «{first['Subject']}»")

    # 2. il collegamento porta alla nuova password (a volte dopo una pagina «procedi»)
    # l'host nel collegamento è quello visto da Keycloak: si tiene il percorso, sull'indirizzo della prova
    target = urllib.parse.urlsplit(html.unescape(action.group(1)))
    print(f"   collegamento verso {target.scheme}://{target.netloc}")
    page = get(urllib.parse.urljoin(base, target.path + "?" + target.query))
    if 'name="password-new"' not in page:
        proceed = re.search(r'href="([^"]*login-actions/[^"]+)"', page)
        if not proceed:
            sys.exit("Il collegamento non porta alla nuova password:\n" + page[:2000])
        page = get(urllib.parse.urljoin(base, html.unescape(proceed.group(1))))
    if 'class="nexus-header"' not in page:
        sys.exit("La pagina della nuova password non usa il tema di NEXUS")
    post(urllib.parse.urljoin(base, form(page)),
         {"password-new": NEW_PASSWORD, "password-confirm": NEW_PASSWORD})

    # 3. l'email di sicurezza, una sola
    wait_for_messages(2)
    time.sleep(5)
    second = MESSAGES[1]
    if address not in second["To"]:
        sys.exit(f"Email di sicurezza al destinatario sbagliato: {second['To']}")
    print(f"2. Email di sicurezza: «{second['Subject']}»")
    if len(MESSAGES) != 2:
        sys.exit(f"Attese 2 email, arrivate {len(MESSAGES)}: " + ", ".join(str(m["Subject"]) for m in MESSAGES))
    print("Recupero della password ed email di sicurezza: OK")


if __name__ == "__main__":
    main()
