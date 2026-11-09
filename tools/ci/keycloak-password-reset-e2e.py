"""Prova completa del recupero della password con un Keycloak vero (US-1308), per la CI.

Fa da server di posta (SMTP minimo, solo libreria standard) e da utente:
«Password dimenticata?» -> email con il collegamento; password sbagliata -> nessuna email;
accesso con aggiornamento della password -> email di sicurezza. Esattamente due email in tutto.

Uso: python3 -I keycloak-password-reset-e2e.py <url keycloak> <username> <email> <password attuale>
L'utente deve avere l'azione richiesta UPDATE_PASSWORD (la imposta la CI).
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
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from http.cookiejar import CookieJar, DefaultCookiePolicy

SMTP_PORT = 2525
NEW_PASSWORD = "Una-password-nuova-1308"
# nell'elenco delle password comuni (EN-7), lunga 12 caratteri: la rifiuta solo la policy passwordBlacklist
COMMON_PASSWORD = "password1234"
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


def signed(message: email.message.EmailMessage) -> None:
    """Oggetto con «PORTALE NEXUS - » e firma «Portale Nexus» in fondo, nel testo e nell'HTML (PO, 07/11)."""
    if not str(message["Subject"]).startswith("PORTALE NEXUS - "):
        sys.exit(f"Oggetto senza «PORTALE NEXUS - »: «{message['Subject']}»")
    for kind in ("plain", "html"):
        part = message.get_body(preferencelist=(kind,))
        if part is None or "Portale Nexus" not in part.get_content().strip()[-60:]:
            sys.exit(f"Manca la firma «Portale Nexus» in fondo all'email «{message['Subject']}» ({kind})")


def form(page: str) -> str:
    match = re.search(r'<form[^>]*action="([^"]+)"', page)
    if not match:
        sys.exit("Nessun modulo nella pagina:\n" + page[:2000])
    return html.unescape(match.group(1))


def main() -> None:
    base, username, address, old_password = sys.argv[1:5]
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
    signed(first)
    print(f"1. Email del collegamento: «{first['Subject']}»")

    # 2. una password sbagliata non manda email (il filtro dell'immagine lascia passare solo il cambio di password)
    jar.clear()
    page = get(auth)
    post(urllib.parse.urljoin(base, form(page)), {"username": username, "password": "sbagliata"})
    time.sleep(5)
    if len(MESSAGES) != 1:
        sys.exit("Una password sbagliata ha mandato un'email: " + str(MESSAGES[-1]["Subject"]))
    print("2. Password sbagliata: nessuna email")

    # 3. cambio della password: la CI ha chiesto a Keycloak di farla aggiornare al prossimo accesso
    jar.clear()
    page = post(urllib.parse.urljoin(base, form(get(auth))), {"username": username, "password": old_password})
    if 'name="password-new"' not in page:
        sys.exit("Dopo l'accesso manca la pagina della nuova password:\n" + page[:2000])
    if 'class="nexus-header"' not in page:
        sys.exit("La pagina della nuova password non usa il tema di NEXUS")
    # EN-7: una password tra le più comuni è rifiutata, con il messaggio in italiano
    page = post(urllib.parse.urljoin(base, form(page)),
                {"password-new": COMMON_PASSWORD, "password-confirm": COMMON_PASSWORD})
    if "Password troppo comune" not in html.unescape(page) or 'name="password-new"' not in page:
        sys.exit(f"La password comune «{COMMON_PASSWORD}» non è stata rifiutata:\n" + page[:2000])
    print(f"3a. Password comune «{COMMON_PASSWORD}» rifiutata: «Password troppo comune»")
    try:
        post(urllib.parse.urljoin(base, form(page)),
             {"password-new": NEW_PASSWORD, "password-confirm": NEW_PASSWORD})
    except urllib.error.URLError:
        pass  # accesso completato: Keycloak rimanda alla PWA, che nella CI non c'è

    # 4. l'email di sicurezza, una sola
    wait_for_messages(2)
    time.sleep(5)
    second = MESSAGES[1]
    if address not in second["To"]:
        sys.exit(f"Email di sicurezza al destinatario sbagliato: {second['To']}")
    signed(second)
    text = body(second)
    match = re.search(rf"^{re.escape(username)}, le tue credenziali sono state cambiate il (\d{{2}}/\d{{2}}/\d{{4}}) "
                      r"alle (\d{2}:\d{2}) da questo indirizzo IP: (\S+)\r?\n"
                      r"Se non sei stato tu, per favore contatta l'amministratore\.", text, re.MULTILINE)
    if not match:
        sys.exit("Testo dell'email di sicurezza diverso dal previsto:\n" + text)
    sent_at = datetime.strptime(f"{match.group(1)} {match.group(2)}", "%d/%m/%Y %H:%M").replace(tzinfo=ZoneInfo("Europe/Rome"))
    if abs(datetime.now(ZoneInfo("Europe/Rome")) - sent_at) > timedelta(minutes=3):
        sys.exit(f"Ora dell'email di sicurezza non nel fuso di Roma: {match.group(1)} {match.group(2)}")
    print(f"3. Email di sicurezza: «{second['Subject']}», {match.group(1)} alle {match.group(2)}, IP {match.group(3)}")
    if len(MESSAGES) != 2:
        sys.exit(f"Attese 2 email, arrivate {len(MESSAGES)}: " + ", ".join(str(m["Subject"]) for m in MESSAGES))
    print("Recupero della password ed email di sicurezza: OK")


if __name__ == "__main__":
    main()
