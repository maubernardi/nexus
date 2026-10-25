## ADDED Requirements

### Requirement: Storico e pregressi dell'azienda
Il sistema SHALL mostrare a Call Center e ADMIN, nella scheda dell'azienda, un riepilogo dei pregressi (proposte,
accettate, appuntamenti falliti, tirocini, assunzioni, esiti negativi), con tutte le voci sempre presenti anche a zero,
e l'elenco in sola lettura degli eventi dalla più recente, con tipo, segnalazione collegata, dettaglio, autore e data.
Le esclusioni dell'azienda per una segnalazione SHALL comparire nell'elenco con il motivo. Gli altri ruoli MUST NOT
accedere allo storico.

#### Scenario: Azienda senza eventi
- **WHEN** un operatore apre la scheda di un'azienda senza eventi
- **THEN** il riepilogo mostra tutte le voci a zero e l'elenco indica che non ci sono eventi

#### Scenario: Pregressi
- **WHEN** l'azienda ha ricevuto due proposte, una accettata e conclusa con assunzione, e ha un appuntamento fallito
- **THEN** il riepilogo indica 2 proposte, 1 accettata, 1 appuntamento fallito, 1 assunzione, e ogni evento rimanda alla
  sua segnalazione

#### Scenario: Esclusione
- **WHEN** l'azienda è stata esclusa per una segnalazione con il motivo "Orari non compatibili"
- **THEN** l'elenco mostra "Azienda esclusa" con la segnalazione e il motivo

#### Scenario: Tutor
- **WHEN** un Tutor chiede lo storico di un'azienda
- **THEN** la risposta è `403`
