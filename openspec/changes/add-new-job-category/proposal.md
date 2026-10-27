## Why

Storia **US-302** (Sprint 4), dalla nota operativa 3 del PO: il Tutor deve poter indicare una tipologia di mansione che
non trova nel catalogo, senza bloccare la segnalazione; la decide un **operatore** del Call Center (o l'ADMIN). Se la
respinge, l'operatore sceglie la tipologia corretta e il Tutor ne è avvisato.

## What Changes

- **Invio**: nel modulo "La mansione non è nell'elenco: la descrivo io" → campo "Nuova tipologia di mansione". La
  segnalazione nasce **In attesa di approvazione**; una tipologia già nel catalogo (stesso nome) va scelta dall'elenco.
  Una tipologia del catalogo oppure una nuova, mai entrambe (vincolo `@JobChoice` sul modulo, oltre a
  `ck_ticket_requested_job` nel database).
- **Call Center**: nuova vista della Home **"Da approvare"**; nel dettaglio la sezione "Nuova tipologia da approvare":
  - **approva** (anche correggendo il nome): la tipologia **entra nel catalogo** (codice generato dal nome, univoco) e la
    segnalazione va in coda (`NUOVA`); audit `CATEGORY_APPROVE` e creazione della tipologia (`JOB_CATEGORY`/`CREATE`);
  - **usa una tipologia del catalogo** con il **motivo** obbligatorio: la segnalazione va in coda con quella tipologia;
    audit `CATEGORY_REJECT` con il motivo.
- **Avviso al Tutor** (notifica provvisoria, review dello Sprint 3 §8.2): evidenza "Tipologia cambiata" nella Home e
  avviso nel dettaglio finché il Tutor non apre la segnalazione; in cronologia "Tipologia cambiata dal Call Center" con
  proposta → scelta e motivo.
- Migrazione **V17**: lo stato `IN_ATTESA_APPROVAZIONE_ADMIN` diventa **`IN_ATTESA_APPROVAZIONE`** (dati e vincoli di
  `ticket` e `ticket_status_history`); colonna `category_change_unseen`. Il vecchio nome resta leggibile negli eventi
  di audit, che non si modificano.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-submission`: nuova tipologia proposta dal Tutor e decisione del Call Center.
- `ticket-list`: vista "Da approvare" ed evidenza per il Tutor.
- `domain-model`: stato `IN_ATTESA_APPROVAZIONE`.
