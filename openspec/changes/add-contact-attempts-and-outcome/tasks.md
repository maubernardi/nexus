## 1. Database e backend

- [x] 1.1 `V57__contact_request_attempts.sql`: tentativi, chiusura, avviso verso la richiesta; vincoli con nome e
      `DomainConstraintsIT` (verifica: test verde)
- [x] 1.2 Entità, repository e DTO della pagina della richiesta; `GET /contact-requests/{id}` con i permessi
      (verifica: `ContactRequestResourceIT`)
- [x] 1.3 Tentativi e chiusura, solo per l'operatore che ha la richiesta in carico; azienda collegata solo attiva;
      audit senza le note (verifica: `ContactRequestResourceIT`)
- [x] 1.4 Avviso `CONTACT_REQUEST_CLOSED` con `{numero}`, `{esito}`, `{nota}`, nell'app e per email con l'oggetto della
      richiesta di contatto (verifica: test delle email e degli avvisi)
- [x] 1.5 Pulizia della demo con i tentativi (verifica: `DemoCleanupIT`)

## 2. Frontend

- [x] 2.1 Pagina della richiesta: dati, tentativi, esito; per l'operatore i moduli del tentativo e della chiusura con
      la conferma e la ricerca dell'azienda (verifica: test con axe)
- [x] 2.2 Numero cliccabile negli elenchi; esito in «Le mie richieste»; avviso che apre la richiesta; testi del
      messaggio (verifica: test)

## 3. Dati dimostrativi, prove e verifiche

- [x] 3.1 Seed: un tentativo sulla richiesta già in carico (verifica: avvio locale)
- [x] 3.2 Prova in Chrome in locale (A26, A47)
- [x] 3.3 Collaudo T-41 in `docs/test/collaudo.md`
- [x] 3.4 `./mvnw verify`; `pnpm lint`, `format:check`, `test --run`, `build`
