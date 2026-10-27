## Why

Storia **US-603** (Sprint 5): l'operatore deve poter escludere un'azienda per una segnalazione, con motivo, per non
riproporla allo stesso beneficiario. Dopo un appuntamento fallito l'esclusione non è automatica (review dello Sprint 4,
§8.2): decide l'operatore.

## What Changes

- `POST /api/v1/tickets/{id}/exclusions` (Call Center e ADMIN): azienda e **motivo obbligatorio**; solo su segnalazioni
  prese in carico in `IN_LAVORAZIONE` o `RIAPERTO` (non durante una proposta in corso); un'azienda già esclusa → `409`.
- `GET /api/v1/tickets/{id}/exclusions`: aziende escluse con motivo, autore e data (comprese quelle del rifiuto della
  proposta, US-702).
- L'azienda esclusa non compare più tra le mansioni compatibili della segnalazione; l'esclusione va nell'audit della
  segnalazione (cronologia "Azienda esclusa dal Call Center" con il motivo) e nello storico dell'azienda.
- Interfaccia: "Escludi questa azienda" con il motivo nella finestra "Dettagli" (US-604); elenco "Aziende escluse"
  sotto le compatibili.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-matching`: esclusione di un'azienda.
