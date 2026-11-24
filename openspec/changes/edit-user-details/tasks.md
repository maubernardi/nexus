## 1. Keycloak

- [x] 1.1 Realm di produzione (`infra/deploy/keycloak/nexus-realm.json`) e di sviluppo (`infra/keycloak`):
      `editUsernameAllowed` e profilo utente con username, email, nome e cognome modificabili solo dall'admin (D5);
      verifica: il realm di produzione si importa con le regole (prova locale come la CI) e quello di sviluppo nell'IT
      con Keycloak vero
- [x] 1.2 `infra/deploy/keycloak-user-profile.sh` una tantum, idempotente (D6); verifica: `bash -n` e
      `tools/ci/keycloak-user-profile-e2e.sh`, su un server finto con il realm di prima. Prima dello script l'utente
      cambia nome ed email dalla pagina dell'account; lo script gira due volte; dopo, i tre cambi sono rifiutati e
      l'amministrazione corregge lo username (riuscita in locale il 24/11)
- [x] 1.3 CI: il job «Tema di Keycloak» controlla le regole di D5 nel realm di produzione e lancia la prova dello
      script

## 2. Backend

- [x] 2.1 `IdentityProviderUsers`: `hasPassword` e `updateDetails`, versione Keycloak e versione locale; IT con il
      Keycloak vero (correzione, conflitti, password)
- [x] 2.2 `UserAdminService.details` e `updateDetails` (D1–D4): unici, username solo senza password, audit
      `DETAILS_CHANGED`, Keycloak per ultimo, invito di nuovo o avviso al vecchio indirizzo, richiesta di US-110
      annullata; provati dall'IT di 2.3, con Keycloak simulato
- [x] 2.3 `GET` e `PUT /api/v1/users/{id}/details` solo per l'ADMIN; IT per 403, 400, audit e richiesta senza
      cambiamenti; `./mvnw verify` verde (246 test unitari, 575 d'integrazione)

## 3. Frontend

- [x] 3.1 API e hook dei dati dell'utente; pagina «Dati dell'utente» (`/utenti/:id/dati`) e pulsante «Dati»
      nell'elenco, con l'esito al ritorno (D7); testi in `it.ts`
- [x] 3.2 Test: caricamento, username in sola lettura, errori sui campi, 503, esito con e senza invito, axe;
      `pnpm lint`, `format:check`, `test` e `build` verdi

## 4. Prove e documenti

- [x] 4.1 `collaudo-e2e.py`: l'ADMIN corregge username ed email di un utente nuovo; nuovo invito in Mailpit e vecchio
      collegamento non più valido («Indirizzo email non valido»); riuscita due volte sul collaudo del PC il 24/11
- [x] 4.2 Prova in Chrome sul collaudo del PC (A26), con dati finti: correzione, sola lettura, axe nei temi chiaro e
      scuro e su mobile. Fatto il 24/11: sola lettura con il motivo collegato, errore sul campo con il riepilogo
      a fuoco, contrasti nei due temi (8,8–15,9:1), pagina dell'account di Keycloak in sola lettura; axe nei test della
      pagina. Corretti: pulsanti di «Utenti» che vanno a capo (l'email era schiacciata a 58 px) e username in sola
      lettura non più «obbligatorio». La vista da smartphone non si è potuta provare (finestra di Chrome che non si
      restringe): la prova il PO in T-34.2
- [x] 4.3 Collaudo T-34; README (script una tantum); backlog US-116 e sprint 17
- [x] 4.4 Collaudo T-34 dal PO sul suo PC, poi spunte nella PR prima del merge (T-34.1 … T-34.4 riuscite il 24/11;
      T-34.5 in produzione dopo il deploy)
