## Why

Storia **US-402** (Sprint 2): l'operatore del Call Center deve prendere in carico una segnalazione della coda, così due
operatori non lavorano lo stesso caso; se un collega l'ha presa un istante prima, deve saperlo.

## What Changes

- Transizione `TAKE_CHARGE` (Call Center, ADMIN) da `NUOVA` o `IN_LAVORAZIONE` a `IN_LAVORAZIONE`, con assegnazione
  dell'operatore. Una segnalazione già assegnata o con versione superata → `409`.
- Il motore delle transizioni accetta "effetti" propri della transizione (qui l'assegnazione), applicati dopo i
  controlli e registrati nell'audit insieme al cambio di stato.
- API `POST /api/v1/tickets/{id}/take-charge` (corpo: `version`) e `GET /api/v1/tickets/assigned-to-me`.
- Coda: pulsante "Prendi in carico" per riga; esito (riuscita o conflitto) in un messaggio che riceve il focus, coda
  riallineata. Nuova pagina **Le mie lavorazioni** (segnalazioni aperte assegnate all'operatore), con voce di menu e
  azione in Home.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `cc-queue`: presa in carico e lavorazioni dell'operatore.

## Non-goals

- Rilascio di una presa in carico o riassegnazione a un collega (da valutare con il PO).
- Ricerca e abbinamento della mansione (US-601, US-602).
