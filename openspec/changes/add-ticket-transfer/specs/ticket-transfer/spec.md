## ADDED Requirements

### Requirement: Spostamento delle segnalazioni tra Tutor
L'ADMIN SHALL spostare una o più segnalazioni aperte di un Tutor su un altro Tutor attivo assegnato allo stesso
progetto. Con la segnalazione MUST passare al nuovo Tutor anche il beneficiario; tirocinio e documenti seguono la
segnalazione. Le segnalazioni chiuse MUST NOT essere spostate. Ogni spostamento SHALL essere registrato nell'audit della
segnalazione e del beneficiario, comparire nella cronologia della segnalazione e generare una notifica per il nuovo
Tutor.

#### Scenario: Segnalazioni aperte di un Tutor
- **WHEN** l'ADMIN apre le segnalazioni di un Tutor
- **THEN** vede quelle aperte raggruppate per progetto e, per ogni progetto, i Tutor attivi a cui può spostarle

#### Scenario: Spostamento
- **WHEN** l'ADMIN sposta due segnalazioni di GOL dal Tutor A al Tutor B, assegnato a GOL
- **THEN** le segnalazioni e i loro beneficiari sono di B, la cronologia mostra «Passata a un altro Tutor: A → B» e B
  riceve una notifica per ciascuna

#### Scenario: Tutor non valido
- **WHEN** il Tutor scelto è disattivato, non è assegnato al progetto o ha già la segnalazione
- **THEN** lo spostamento è rifiutato e nulla cambia

#### Scenario: Segnalazione chiusa o di progetti diversi
- **WHEN** tra le segnalazioni scelte ce n'è una chiusa, oppure sono di progetti diversi
- **THEN** lo spostamento è rifiutato e nulla cambia

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor o un operatore prova a spostare una segnalazione
- **THEN** la risposta è `403`
