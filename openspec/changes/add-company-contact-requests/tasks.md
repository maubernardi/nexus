## 1. Database e backend

- [x] 1.1 `V56__contact_request.sql`: estensione `pg_trgm`, contatore `contact_request_number_seq`, tabella
      `contact_request` con vincoli e FK con nome; `DomainConstraintsIT` (verifica: test verde)
- [x] 1.2 Entità `ContactRequest`, repository, tipo `CONTACT_REQUEST` dell'audit (verifica: compilazione)
- [x] 1.3 Servizio e risorsa `/api/v1/contact-requests`:
      - invio, solo per il Tutor;
      - «Le mie richieste»;
      - elenco del Call Center e numero delle richieste aperte;
      - presa in carico con controllo della versione;
      - audit.

      Verifica: `ContactRequestResourceIT`.
- [x] 1.4 Nomi simili `/api/v1/companies/directory/similar` con `pg_trgm`, solo aziende attive e dati essenziali
      (verifica: test nel `ContactRequestResourceIT`)

## 2. Frontend

- [x] 2.1 API e hook delle richieste; voci del menu «Le mie richieste» (Tutor) e «Richieste di contatto» (Call
      Center, ADMIN) con il numero (verifica: test di `MainNav`)
- [x] 2.2 Pagina «Le mie richieste» e pagina della richiesta per un'azienda nuova, con i nomi simili prima dell'invio
      (verifica: test con axe)
- [x] 2.3 Niente «Chiedi di contattarla» nella scheda del Tutor: solo aziende non in anagrafica (PO, 27/11)
- [x] 2.4 Pagina «Richieste di contatto» del Call Center con «Prendi in carico» (verifica: test con axe)

## 3. Dati dimostrativi, prove e verifiche

- [x] 3.1 Seed: due richieste di `tutor1`, una aperta e una in carico (verifica: avvio locale)
- [x] 3.2 Prova in Chrome in locale, con nomi lunghi come quelli veri (A26, A47)
- [x] 3.3 Collaudo T-40 in `docs/test/collaudo.md`
- [x] 3.4 `./mvnw verify`; `pnpm lint`, `format:check`, `test --run`, `build`
