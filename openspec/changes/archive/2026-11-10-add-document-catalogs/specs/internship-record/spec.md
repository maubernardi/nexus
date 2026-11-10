## ADDED Requirements

### Requirement: Tipi ammessi dal progetto
La scheda del tirocinio SHALL proporre solo i tipi di tirocinio attivi ammessi dal progetto della segnalazione, più il
tipo già scelto. Il sistema MUST rifiutare un tipo nuovo che il progetto non ammette; un tipo già scelto MUST restare
valido anche se nel frattempo il progetto non lo ammette più.

#### Scenario: Segnalazione di GOL
- **WHEN** il Tutor apre la scheda del tirocinio di una segnalazione di GOL
- **THEN** può scegliere solo Extracurriculare e Di inclusione

#### Scenario: Tipo non ammesso
- **WHEN** il Tutor salva la scheda di una segnalazione di GOL con il tipo Curriculare
- **THEN** il campo segnala che il tipo non è ammesso dal progetto e nulla è salvato
