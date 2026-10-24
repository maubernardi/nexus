## Why

Storia **US-505** (Sprint 4), dalla nota operativa 2 del PO: gli operatori devono annotare informazioni utili
sull'azienda (orari, preferenze, chiusure) e ritrovarle negli abbinamenti futuri, anche nel popup di US-604.

## What Changes

- Nuova tabella `company_note` (migrazione **V15**) in **sola aggiunta**: il database rifiuta modifica, cancellazione e
  svuotamento, come per lo storico dell'azienda; testo non vuoto (`ck_company_note_text`), al massimo 2000 caratteri.
- API `GET` e `POST /api/v1/companies/{id}/notes`, solo Call Center e ADMIN: elenco dalla più recente con data, ora e
  nome dell'autore; aggiunta con testo obbligatorio.
- Audit: evento `NOTE_ADD` sull'azienda **senza il testo**, che può contenere dati personali (come EN-4).
- Sezione **"Note"** nella scheda azienda: campo "Nuova nota", "Aggiungi nota", elenco ordinato; nessun comando di
  modifica o cancellazione.
- Dati demo: alcune note fittizie sulle aziende demo.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: diario delle note dell'azienda.
