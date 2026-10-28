## Why

Storia **US-803** (Sprint 6): dopo un appuntamento fallito il Tutor chiede il rilancio della mansione in bacheca, per
offrirla ad altri beneficiari. Decisione del PO (review dello Sprint 5, §8.1): una casella nel modulo
dell'appuntamento fallito crea una bozza riservata al progetto della segnalazione; il Call Center riceve una notifica
e decide se pubblicarla.

## What Changes

- `POST /tickets/{id}/appointment-failed` accetta `relaunch` (facoltativo, `false` se manca). Con il rilancio, nella
  stessa transazione dell'esito:
  - nasce una **bozza** con la mansione appena liberata e il suo titolo, riservata al progetto della segnalazione e
    collegata a essa (`created_from_ticket_id`);
  - l'audit registra `CREATE` con il motivo "Rilancio dopo l'appuntamento fallito della segnalazione n. N";
  - l'operatore che ha in carico la segnalazione riceve la notifica `BOARD_RELAUNCH_REQUESTED`, oltre a
    `APPOINTMENT_FAILED`;
  - in cronologia la causa è seguita da "rilancio in bacheca richiesto".
- Il dettaglio della segnalazione indica l'ultimo annuncio nato dal rilancio (`relaunchPostId`, `relaunchPostNumber`,
  `relaunchPostStatus`).
- Frontend:
  - la casella "Chiedi il rilancio della mansione in bacheca" nel modulo, con la spiegazione;
  - l'esito dice che il rilancio è stato chiesto;
  - la sezione "Rilancio in bacheca" nel dettaglio: il link alla bozza per il Call Center, numero e stato per il
    Tutor;
  - il testo della nuova notifica.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-board`: rilancio in bacheca dopo un appuntamento fallito.
