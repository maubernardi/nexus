## 1. Backend

- [x] 1.1 Migrazione V27 `email_change_request` con vincoli nominati; entità e repository; test dei vincoli
- [x] 1.2 Servizio: richiesta (validazioni, codice, hash, invio), conferma (tentativi, scadenza, Keycloak, audit), annullamento
- [x] 1.3 Email: codice al nuovo indirizzo (sincrona), avviso al vecchio dopo il commit; codice nel log solo in locale
- [x] 1.4 Client della API di Keycloak (`client_credentials`, lettura e riscrittura dell'utente); versione vuota fuori da `keycloak`
- [x] 1.5 Endpoint `/me/email`, `/me/email-change`, `/confirm`; test di integrazione del flusso e test unitari

## 2. Frontend

- [x] 2.1 API e hook; pagina «Il mio profilo» con i due passi, reinvio, annullamento; collegamento dal nome
- [x] 2.2 Testi in `it.ts`; test con axe

## 3. Keycloak e deploy

- [x] 3.1 Client `nexus-backend-admin` nei realm (sviluppo con segreto di sviluppo, demo con segreto da variabile)
- [x] 3.2 Compose e `bootstrap.sh`: segreto `NEXUS_KEYCLOAK_ADMIN_CLIENT_SECRET`; script una tantum per il server esistente
- [x] 3.3 README e collaudo

## 4. Consegna

- [ ] 4.1 PR con CI verde
- [ ] 4.2 Sul server: script del client Keycloak, poi prova reale del cambio dopo il deploy
