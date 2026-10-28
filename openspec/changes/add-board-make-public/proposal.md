## Why

Storia **US-903** (Sprint 6): se nessuno risponde a un annuncio riservato a un progetto, il Call Center lo rende
visibile ai Tutor di tutti i progetti. Regola del brief: solo quando la data corrente è almeno la data di
pubblicazione più 7 giorni solari. Lo sblocco registra la data.

## What Changes

- `POST /api/v1/board-posts/{id}/make-public`, per Call Center e ADMIN, con il blocco ottimistico:
  - solo su un annuncio **pubblicato e riservato**, e solo da 7 giorni solari dopo la pubblicazione;
  - altrimenti risponde `409`, indicando da quando si potrà fare;
  - toglie il progetto, registra `made_public_at` e va nell'audit (`BOARD_POST` `MAKE_PUBLIC`).
- `BoardPostDTO.makePublicFrom`: da quando lo sblocco è possibile, calcolato dall'entità (`BoardPost.makePublicFrom`).
- Frontend, nella scheda dell'annuncio:
  - "Rendi visibile a tutti" con conferma, solo da quella data;
  - prima della data, una riga dice da quando si potrà fare;
  - dopo lo sblocco, lo stato riporta "Visibile a tutti dal …".

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-board`: sblocco a tutti dopo 7 giorni.
