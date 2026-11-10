## ADDED Requirements

### Requirement: Correzione della sede di competenza
Il dettaglio della segnalazione SHALL mostrare la sede di competenza. Dopo la creazione il Tutor MUST NOT cambiarla;
l'ADMIN e il Coordinatore del progetto della segnalazione SHALL poterla correggere con una sede attiva del progetto.
Ogni correzione SHALL essere nell'audit della segnalazione, con la sede prima e dopo.

#### Scenario: Correzione del Coordinatore
- **WHEN** il Coordinatore di GOL corregge la sede di una segnalazione di GOL
- **THEN** la sede è cambiata e la cronologia della segnalazione mostra la correzione

#### Scenario: Tutor non coordinatore
- **WHEN** il Tutor della segnalazione prova a cambiare la sede
- **THEN** la risposta è `403`
