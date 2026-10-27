## Why

Storia **US-705** (Sprint 5): se alla fine del 7° giorno lavorativo dall'accettazione l'appuntamento non è registrato,
la mansione non deve restare bloccata. Decisioni: Q2 (la segnalazione va in `RIAPERTO`), review dello Sprint 4 §8.2
(resta allo stesso operatore), planning dello Sprint 5 (controllo ogni ora, scadenza a mezzanotte di Roma).

## What Changes

- **Transizioni del sistema**: `TicketTransition.bySystem(...)` senza ruoli; la macchina a stati le applica solo con
  `applyBySystem` e senza utente collegato; nessun utente può richiederle dall'API.
- `TIMER_EXPIRE`: `PROPOSTA_ACCOLTA` → `RIAPERTO`. `TimerService.expireDue(adesso)`: per le proposte accettate con
  scadenza raggiunta azzera il timer, libera la mansione (audit `RELEASE`), lascia la segnalazione allo stesso
  operatore; storico degli stati e audit (autore "Sistema"); notifica `TIMER_EXPIRED` al **Tutor** e all'**operatore**.
- Il controllo orario esegue **prima le scadenze e poi i promemoria**: una segnalazione scaduta non riceve il
  promemoria.
- Testi: notifica della scadenza e voce "Scadenza del timer" in cronologia.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: scadenza del timer.
