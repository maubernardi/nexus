## Context

Motivazione: proposal.md (EN-2). Modello esistente: `company_audit_event` già in sola aggiunta con trigger e dettagli
`jsonb`; `TicketStateMachine` è il punto unico dei cambi di stato.

## Decisions

### D1 — Un registro generico
`audit_event(id, entity_type, entity_id, action, changes jsonb, reason, created_at, created_by)`. Niente FK verso le
tabelle degli oggetti: il registro deve sopravvivere a qualunque oggetto e non bloccarne l'anonimizzazione. Vincoli:
`entity_type` e `action` in `MAIUSCOLO_CON_UNDERSCORE`, `changes` oggetto JSON. Indici per oggetto e per autore, in
ordine di tempo. Stesso schema di immutabilità di `company_audit_event` (trigger).

### D2 — Forma di `changes`
Mappa campo → `{"before": …, "after": …}` per i valori in chiaro, campo → `{"redacted": true}` per i dati personali.
Costruita con `AuditChanges` (`value(campo, prima, dopo)`, `redacted(campo)`); i valori sono stringhe o numeri già
serializzati (id esterni TSID, nomi di enum).

### D3 — Azioni
`action` è una stringa: per le transizioni del ticket è il nome della transizione (`SUBMIT`, poi `TAKE_CHARGE`, …), per
il resto verbi come `CREATE`, `UPDATE`, `DEACTIVATE`. `entity_type` è un enum (`AuditEntityType`).

### D4 — Transazione
`AuditService.record` ha `@Transactional(propagation = MANDATORY)`: l'evento si salva con l'operazione o per niente.

### D5 — Collegamenti iniziali
- `TicketStateMachine.create/apply`: evento sul ticket con lo stato prima/dopo e la nota come motivo; alla creazione anche
  le scelte (beneficiario, progetto, mansione richiesta) come id esterni.
- `BeneficiaryService.register`: evento `CREATE` sul beneficiario, senza valori.
