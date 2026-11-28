## 1. Backend

- [x] 1.1 `PUT /tickets/{id}/request`: solo il Tutor, stati prima della proposta, segnalazioni normali, versione;
      progetto con sede e tipo validi; tipologia attiva o testo libero in attesa di approvazione (verifica:
      `TicketRequestCorrectionIT`)
- [x] 1.2 `canCorrectRequest` nel dettaglio; audit `REQUEST_CORRECTED` con prima e dopo e voce nello storico
      (verifica: test)

## 2. Frontend

- [x] 2.1 «Correggi progetto e mansione» nel dettaglio, con il modulo, l'esito annunciato e la voce nello storico
      (verifica: test con axe)

## 3. Prove e verifiche

- [x] 3.1 Prova in Chrome in locale (A26)
- [x] 3.2 Collaudo T-42 in `docs/test/collaudo.md`
- [x] 3.3 `./mvnw verify`; `pnpm lint`, `format:check`, `test --run`, `build`
