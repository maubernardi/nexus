## ADDED Requirements

### Requirement: Presa in carico
Il sistema SHALL permettere a Call Center e ADMIN di prendere in carico una segnalazione della coda indicando la versione
vista: il ticket passa in `IN_LAVORAZIONE`, è assegnato all'operatore, esce dalla coda e l'azione è registrata nella
cronologia e nell'audit. Se la segnalazione è già assegnata o è cambiata nel frattempo, il sistema SHALL rispondere
`409` senza modificarla.

#### Scenario: Presa in carico riuscita
- **WHEN** un operatore prende in carico una segnalazione `NUOVA`
- **THEN** il ticket è `IN_LAVORAZIONE`, assegnato a lui, non compare più nella coda e l'audit registra `TAKE_CHARGE`
  con stato prima/dopo e operatore assegnato

#### Scenario: Collega più veloce
- **WHEN** due operatori prendono in carico la stessa segnalazione partendo dalla stessa versione
- **THEN** il primo riesce e il secondo riceve `409` e vede un messaggio che lo spiega, con la coda aggiornata

#### Scenario: Ruolo non autorizzato
- **WHEN** un Tutor tenta la presa in carico
- **THEN** la risposta è `403`

### Requirement: Le mie lavorazioni
Il sistema SHALL mostrare all'operatore le segnalazioni aperte assegnate a lui, dalla più vecchia, in una tabella
accessibile come quella della coda.

#### Scenario: Dopo la presa in carico
- **WHEN** l'operatore ha preso in carico una segnalazione
- **THEN** la trova in "Le mie lavorazioni", e un altro operatore non la vede tra le proprie
