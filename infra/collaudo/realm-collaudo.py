"""NEXUS — realm del collaudo (EN-11), dal realm di produzione del commit da provare.

Tiene tutto quello della produzione (policy delle password, blocco dopo i tentativi falliti, durata delle sessioni, tema,
ruoli, client) e cambia solo:
  - gli indirizzi della PWA: http://localhost:8090 invece di https://<dominio>;
  - la posta: la casella finta Mailpit (mailpit:1025), senza TLS né password;
  - gli utenti: quelli finti del realm di sviluppo (stessi id del seed di NEXUS), con la password del collaudo, e
    l'account tecnico del backend.

Uso: python3 -I realm-collaudo.py <realm di produzione> <realm di sviluppo> <realm del collaudo> <password>
"""

import json
import sys

APP_URL = "http://localhost:8090"


def main() -> None:
    production_path, development_path, output_path, password = sys.argv[1:5]
    with open(production_path, encoding="utf-8") as f:
        realm = json.load(f)
    with open(development_path, encoding="utf-8") as f:
        development = json.load(f)

    for client in realm["clients"]:
        if client["clientId"] == "nexus-frontend":
            client["redirectUris"] = [f"{APP_URL}/*"]
            client["webOrigins"] = [APP_URL]
            client.setdefault("attributes", {})["post.logout.redirect.uris"] = f"{APP_URL}/*"

    smtp = realm.get("smtpServer", {})
    realm["smtpServer"] = {
        "host": "mailpit",
        "port": "1025",
        "from": smtp.get("from", "noreply@portalenexus.it"),
        "fromDisplayName": smtp.get("fromDisplayName", "NEXUS"),
        "starttls": "false",
        "ssl": "false",
        "auth": "false",
    }

    users = []
    for user in development["users"]:
        user = dict(user)
        if "credentials" in user:
            user["credentials"] = [{"type": "password", "value": password, "temporary": False}]
        users.append(user)
    if not any(u.get("credentials") for u in users):
        sys.exit("nessun utente con password nel realm di sviluppo")
    realm["users"] = users

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(realm, f, ensure_ascii=False, indent=2)
        f.write("\n")


if __name__ == "__main__":
    main()
