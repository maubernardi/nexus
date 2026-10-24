## Why

Storia **US-405** (Sprint 3), decisione del PO nella review dello Sprint 2 (§7.2, 05/10): "se manca un operatore, l'altro
può operare anche sulle sue segnalazioni". La regola dello Sprint 2 (abbina solo chi l'ha in carico) era troppo rigida.

## What Changes

- Una segnalazione presa in carico si lavora e si abbina da **qualunque** operatore del Call Center o dall'ADMIN;
  l'assegnazione **non cambia**, audit e cronologia registrano chi agisce. Una segnalazione non ancora presa in carico
  va prima presa dalla coda (anche per l'ADMIN, per coerenza con la coda).
- `POST /api/v1/tickets/{id}/take-over` ("Prendila in carico tu"): l'operatore corrente diventa l'assegnatario, con
  versione attesa e audit `TAKE_OVER` (operatore prima/dopo).
- Pagina di lavorazione: per le segnalazioni di un collega mostra "In carico a …", la spiegazione e il pulsante
  "Prendila in carico tu"; la ricerca delle compatibili è disponibile.
- La vista "Tutte" con la colonna dell'operatore arriva con la nuova Home (US-303, nota 1 del PO), per non costruire due
  elenchi uguali.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-matching`: chi può abbinare.
