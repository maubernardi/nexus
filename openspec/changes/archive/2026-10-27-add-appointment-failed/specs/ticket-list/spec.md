## ADDED Requirements

### Requirement: Riaperte in cima
Nella vista "In carico a me" le segnalazioni `RIAPERTO` SHALL comparire per prime, con lo stato evidenziato in testo,
seguite dalle altre dalla più vecchia.

#### Scenario: Riaperta in cima
- **WHEN** una segnalazione dell'operatore viene riaperta
- **THEN** compare in cima a "In carico a me" con lo stato "Riaperto" evidenziato
