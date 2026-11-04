## ADDED Requirements

### Requirement: Cataloghi del report di restituzione
L'ADMIN SHALL gestire le formule di assunzione e i motivi della mancata assunzione come gli altri cataloghi:
aggiunta, nuovo nome, disattivazione e riattivazione, con l'audit. Il sistema SHALL partire con le voci provvisorie
indicate dal PO (Q6). Il report di restituzione SHALL proporre solo le voci attive.

#### Scenario: Voci iniziali
- **WHEN** l'ADMIN apre la pagina "Cataloghi"
- **THEN** trova le formule (tempo determinato 3, 6 e 12 mesi, tempo indeterminato, contratto a chiamata, prestazione
  occasionale) e i tre motivi della mancata assunzione

#### Scenario: Motivo disattivato
- **WHEN** l'ADMIN disattiva un motivo della mancata assunzione
- **THEN** il motivo non è più tra quelli proposti per il report, ma resta sui report che lo usano
