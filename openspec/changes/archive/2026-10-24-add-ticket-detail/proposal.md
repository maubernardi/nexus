## Why

Storia **US-305** (Sprint 3): Tutor e Call Center devono vedere il dettaglio della segnalazione e la cronologia (data,
autore, nota per ogni cambio) per sapere cosa è successo e quando. Con la Home (US-303) il Tutor ha l'elenco ma non può
ancora aprire una segnalazione.

## What Changes

- `GET /api/v1/tickets/{id}` (al posto di `/work`) per tutti i ruoli, con visibilità: Call Center/ADMIN tutte, Tutor le
  proprie e quelle dei suoi progetti; altrimenti `404`.
- `GET /api/v1/tickets/{id}/history`: cronologia dalla più vecchia, costruita dall'audit (EN-2) con dettagli leggibili
  (operatore della presa in carico, passaggio a un collega, «mansione» presso azienda) e autori per nome; per le
  segnalazioni nate prima dell'audit, i cambi di stato mancanti vengono dallo storico degli stati.
- Pagina unica **`/segnalazioni/:id`** per tutti i ruoli: dati, proposta, cronologia; per Call Center/ADMIN anche la
  lavorazione (presa in carico da un collega, compatibili, abbinamento). `/lavorazioni/:id` reindirizza. La Home apre
  il dettaglio per ogni ruolo.
- Il numero nella tabella della Home ha un `aria-label` ("Apri la segnalazione n. N") invece di un testo nascosto, che
  spostava l'area cliccabile.

## Capabilities

### New Capabilities
- `ticket-detail`: dettaglio e cronologia della segnalazione.

### Modified Capabilities
<!-- nessuna -->
