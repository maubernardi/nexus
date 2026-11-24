## 1. Ambiente di collaudo

- [x] 1.1 `infra/collaudo/compose.yaml`: override dello stack della produzione, con indirizzi locali, porte solo su
      127.0.0.1 con `!override` (mai la 8081), backend `keycloak,seed` e posta verso Mailpit (D1, D6)
- [x] 1.2 `infra/collaudo/Caddyfile`, con le regole della produzione in HTTP su localhost (D3)
- [x] 1.3 `infra/collaudo/realm-collaudo.py`: realm del collaudo dal realm di produzione, con gli utenti finti del
      realm di sviluppo (D4)
- [x] 1.4 `infra/collaudo/collaudo.sh`: `avvia` (PR, branch o commit; sorgenti con `git archive` fuori dal checkout;
      build saltata se le immagini esistono; sempre dai dati finti; controllo delle porte), `ferma`, `riprendi`,
      `stato`, `cancella` (D2, D5)

## 2. Prove

- [x] 2.1 `tools/ci/collaudo-e2e.py`: accesso di `tutor1` con PKCE e `/api/v1/me`, email del backend e di Keycloak in
      Mailpit (D7)
- [x] 2.2 Job «Ambiente di collaudo» nella CI, con la verifica che non restino volumi (A37); Dependabot segue Mailpit
- [x] 2.3 Prova sul PC, il 24/11, su `main` (`388e8c2`):
      - avvio con la prima build in circa 5 minuti;
      - `collaudo-e2e.py` riuscito;
      - `ferma` e `riprendi` con i controlli verdi; `cancella` senza container, volumi, immagini né cartelle;
      - 8081 mai aperta.

      Corretto: Keycloak sano rispondeva 503 per qualche secondo, mentre finiva di importare il realm. Ora i controlli
      riprovano per un minuto.
- [x] 2.4 Prova in Chrome dal PC (A26): pagina di accesso con il tema su http://localhost:8091, accesso di
      `operatore.cc`, segnalazioni e aziende finte, PWA pronta offline, casella di Mailpit con le email della prova

## 3. Documenti e processo

- [x] 3.1 README «Collaudo sul PC»; Definition of Done e flusso della storia (`docs/agile/README.md`, D8)
- [x] 3.2 Collaudo: ambiente e preparazione, T-33; backlog EN-11 e sprint 17
- [ ] 3.3 Collaudo T-33 dal PO, poi spunte nella PR prima del merge
