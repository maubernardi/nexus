## Why

Storia **US-107** (Sprint 4): le scadenze (timer di 7 giorni lavorativi, US-701/US-703) devono saltare il patrono e le
chiusure della cooperativa. Decisione del PO nella review dello Sprint 3: il calendario è **unico per tutti** i
progetti, non per progetto.

## What Changes

- Migrazione **V16**:
  - `work_calendar`, una sola riga (`ck_work_calendar_single_row`) con il patrono, inizialmente il **24 giugno**, e la
    versione per il blocco ottimistico;
  - `calendar_closure`, chiusure come giorno o periodo con descrizione (`ck_calendar_closure_period`,
    `ck_calendar_closure_description`).
- API per l'**ADMIN**: `GET /api/v1/calendar`, `PUT /calendar/patron`, `POST /calendar/closures`,
  `DELETE /calendar/closures/{id}`. Audit sull'oggetto `CALENDAR` con prima e dopo (non sono dati personali):
  `PATRON_UPDATE`, `CLOSURE_ADD`, `CLOSURE_REMOVE`.
- Pagina **"Calendario"** (menu dell'ADMIN): patrono (giorno e mese), elenco informativo delle festività nazionali,
  chiusure con aggiunta e rimozione.
- Le festività nazionali non si configurano: le calcola il codice (US-703).

## Capabilities

### New Capabilities
- `work-calendar`: calendario unico dei giorni lavorativi.

### Modified Capabilities
<!-- nessuna -->
