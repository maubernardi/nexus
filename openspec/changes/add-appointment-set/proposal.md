## Why

Storia **US-801** (Sprint 5): dopo l'accettazione il Tutor contatta l'azienda; quando l'appuntamento è fissato lo
registra, così il percorso avanza e il timer di 7 giorni lavorativi non scade più.

## What Changes

- Transizione `APPOINTMENT_SET`: `PROPOSTA_ACCOLTA` → `APPUNTAMENTO`, solo per il **Tutor della segnalazione**
  (`POST /api/v1/tickets/{id}/appointment-set`, con versione e nota facoltativa, es. data e ora).
- **Il timer si ferma**: inizio, promemoria e scadenza si azzerano; i valori restano nell'audit. La mansione resta
  bloccata.
- Cronologia "Appuntamento fissato – Timer fermato" con la nota; evento `APPUNTAMENTO_FISSATO` nello storico
  dell'azienda; notifica `APPOINTMENT_SET` all'operatore che ha in carico la segnalazione.
- Dettaglio: "Appuntamento fissato" con conferma e nota facoltativa nella sezione della proposta.
- Correzione: l'esito di un'azione resta visibile anche quando, ricaricata la segnalazione, il pulsante dell'azione
  sparisce (azione asincrona non legata al componente).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: appuntamento fissato.
