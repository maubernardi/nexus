## Why

Storia **US-904** (Sprint 6): il Call Center crea un annuncio in bacheca partendo da una mansione libera, per
promuovere le posizioni aperte. Una mansione può avere un solo annuncio attivo (bozza o pubblicato).

## What Changes

- `POST /api/v1/board-posts` (`{jobSlotId}`), solo per Call Center e ADMIN.
  - Crea una **bozza** con il titolo della mansione, visibile a tutti i progetti.
  - Richiede una mansione libera e a disposizione, di un'azienda attiva (le stesse condizioni della pubblicazione,
    US-902).
  - Se la mansione ha già un annuncio attivo risponde `409`, con il numero di quell'annuncio.
  - La creazione va nell'audit (`BOARD_POST` `CREATE`).
- Le mansioni nella scheda dell'azienda indicano l'annuncio attivo (`activeBoardPostId`, `activeBoardPostNumber`).
- Frontend, nella scheda dell'azienda:
  - "Crea annuncio" accanto a ogni mansione libera e a disposizione;
  - "Annuncio #N" se l'annuncio esiste già.
  Dopo la creazione si apre la scheda dell'annuncio, con l'esito; la bacheca spiega da dove si creano gli annunci.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-board`: creazione di un annuncio da una mansione libera.
