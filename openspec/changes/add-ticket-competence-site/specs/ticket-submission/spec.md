## ADDED Requirements

### Requirement: Sede di competenza alla creazione
Creando una segnalazione, normale o dalla bacheca, il Tutor SHALL scegliere la sede di competenza tra le sedi attive del
progetto della segnalazione. La sede MUST essere indicata se il progetto ha almeno una sede attiva e MUST appartenere al
progetto; se il progetto non ha sedi il campo non c'è.

#### Scenario: Progetto con sedi
- **WHEN** il Tutor crea una segnalazione di GOL senza indicare la sede
- **THEN** il campo segnala che la sede è obbligatoria e la segnalazione non è creata

#### Scenario: Sede di un altro progetto
- **WHEN** il Tutor indica una sede che non è del progetto della segnalazione
- **THEN** il campo segnala che la sede non è valida

#### Scenario: Progetto senza sedi
- **WHEN** il Tutor crea una segnalazione di un progetto senza sedi
- **THEN** la segnalazione è creata senza sede
