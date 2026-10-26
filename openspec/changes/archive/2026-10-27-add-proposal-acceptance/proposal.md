## Why

Storia **US-701** (Sprint 4): dopo la proposta all'azienda, il Tutor deve accettarla per far partire il contatto con
l'azienda; da quel momento un **timer di 7 giorni lavorativi** (brief, fase 5) presidia i tempi di risposta. Il calcolo
dei giorni lavorativi viene da US-703 sul calendario unico (US-107).

## What Changes

- Transizione `ACCEPT`: `PROPOSTA_AZIENDA` → `PROPOSTA_ACCOLTA`, solo per il **Tutor della segnalazione**
  (`POST /api/v1/tickets/{id}/accept`, con versione): un collega di progetto la vede ma riceve `403`, chi non la vede
  `404`, il Call Center `403`.
- **Timer**: inizio all'accettazione; scadenza alla fine del 7° giorno lavorativo successivo (mezzanotte di Roma del
  giorno dopo), salvata in `timer_deadline_at`. La mansione resta bloccata.
- Audit `ACCEPT` con la scadenza; in cronologia "Proposta accettata – Scadenza gg/mm/aaaa". Evento `PROPOSTA_ACCOLTA`
  nello storico dell'azienda (conta tra le "Accettate").
- Dettaglio della segnalazione: `myTicket` (il Tutor corrente è il titolare) e `timerDeadline` (ultimo giorno del
  timer); nella sezione "Proposta all'azienda" il pulsante **"Accetta la proposta"** con conferma e la **scadenza del
  timer**, visibile a tutti.
- Orologio dell'applicazione con il fuso di Roma (`TimeConfig`).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: accettazione della proposta e timer.
