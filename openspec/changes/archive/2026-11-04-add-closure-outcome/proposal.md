## Why

Storia **US-1102** (Sprint 8). Il report di restituzione (US-1101) dice se la mansione è ancora disponibile e come è
finito il tirocinio. Il Call Center ha bisogno che queste risposte aggiornino da sole la mansione e lo storico
dell'azienda: sono i dati su cui fa i prossimi abbinamenti.

## What Changes

- Con l'invio del report (transizione `CLOSE`):
  - **mansione ancora disponibile**: la mansione torna `LIBERA`, con l'audit del rilascio (`JOB_SLOT`, `RELEASE`);
  - **mansione non più disponibile**: resta `BLOCCATA` dalla segnalazione conclusa;
  - in entrambi i casi la segnalazione resta collegata alla mansione, per lo storico.
- **Storico dell'azienda**: evento `TIROCINIO_CONCLUSO`, in sola aggiunta, con:
  - mansione e numero della segnalazione;
  - assunto sì/no, con la formula o il motivo;
  - mansione ancora disponibile sì/no.

  Il riepilogo conta già assunzioni ed esiti negativi (US-503). Il dettaglio dell'evento diventa, per esempio,
  «Con assunzione: «Tempo indeterminato»; mansione di nuovo libera».
- La cronologia della segnalazione dice «mansione di nuovo libera» invece di «mansione ancora disponibile».
- Il modulo del report spiega che con il "sì" la mansione torna libera.

Nessuna migrazione.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-audit-trail`: esito del tirocinio nello storico e mansione di nuovo libera.
