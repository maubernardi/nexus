## Why

Storia **US-902** (Sprint 6): il Call Center controlla cosa è visibile ai Tutor in bacheca. Vede le bozze, le
modifica, le pubblica (con la data) e archivia gli annunci. È la base delle altre storie dello sprint: annuncio da una
mansione libera (US-904), bacheca per il Tutor (US-901), rilancio (US-803) e sblocco a tutti (US-903).

## What Changes

- API `/api/v1/board-posts`, solo per Call Center e ADMIN:
  - elenco per stato e dettaglio;
  - modifica (`PUT`);
  - `POST /{id}/publish` e `POST /{id}/archive`.
  Ogni azione usa il blocco ottimistico (`version`) e va nell'audit (`BOARD_POST`: `UPDATE`, `PUBLISH`, `ARCHIVE`).
- Campi modificabili: titolo, fascia d'età, ore settimanali, durata in mesi, note. Il progetto riservatario si sceglie
  solo nella bozza; la mansione non cambia.
- Pubblicazione: solo una bozza, con la mansione libera e a disposizione e l'azienda attiva. Registra la data.
- Archiviazione: da bozza o pubblicato; un annuncio archiviato è in sola lettura.
- Frontend:
  - voce di menu "Bacheca" per Call Center e ADMIN;
  - pagina `/bacheca` con le viste "Bozze", "Pubblicati" e "Archiviati";
  - pagina `/bacheca/:id` con i dati della mansione, il modulo e le azioni "Pubblica" e "Archivia", ciascuna con
    conferma.

## Capabilities

### New Capabilities
- `job-board`: gestione degli annunci della bacheca.

### Modified Capabilities
<!-- nessuna -->
