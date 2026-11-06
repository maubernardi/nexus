# ticket-transfer Specification

## Purpose
TBD - created by archiving change add-ticket-transfer. Update Purpose after archive.
## Requirements
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

#### Scenario: Solo l'ADMIN (o il Coordinatore, US-112)
- **WHEN** un operatore, o un Tutor che non coordina il progetto, prova a spostare una segnalazione
- **THEN** la risposta è `403`

### Requirement: Coordinatore di progetto
L'ADMIN SHALL poter nominare Coordinatore di un progetto un Tutor assegnato a quel progetto; un progetto MAY avere più
Coordinatori e lo stesso Tutor MAY coordinare un progetto ed essere solo Tutor di un altro. Il Coordinatore SHALL
spostare le segnalazioni aperte dei progetti che coordina tra i Tutor attivi dello stesso progetto, sé compreso, con le
stesse regole e tracce dello spostamento dell'ADMIN. Il Coordinatore MUST NOT spostare segnalazioni di progetti che non
coordina. Il coordinamento MUST decadere quando il progetto è tolto al Tutor o quando l'utente non è più Tutor.

#### Scenario: Nomina del Coordinatore
- **WHEN** l'ADMIN segna il Tutor A come Coordinatore di GOL, lasciandolo solo Tutor di POLIS
- **THEN** A vede la voce «Coordinamento», l'elenco degli utenti mostra «GOL (Coordinatore)» e l'audit dell'utente
  registra i progetti coordinati prima e dopo

#### Scenario: Progetto non assegnato
- **WHEN** l'ADMIN segna come coordinato un progetto non assegnato al Tutor
- **THEN** la richiesta è rifiutata sul campo e nulla cambia

#### Scenario: Coordinamento
- **WHEN** il Coordinatore di GOL apre il coordinamento
- **THEN** vede tutte le segnalazioni aperte di GOL con il loro Tutor e i Tutor attivi di GOL che possono riceverle,
  sé compreso

#### Scenario: Spostamento del Coordinatore
- **WHEN** il Coordinatore di GOL sposta una segnalazione di GOL dal Tutor B a sé
- **THEN** la segnalazione e il beneficiario sono suoi, con audit, cronologia e notifica come per l'ADMIN

#### Scenario: Progetto non coordinato
- **WHEN** il Coordinatore di GOL prova a spostare una segnalazione di POLIS, di cui è solo Tutor
- **THEN** la risposta è `403` e nulla cambia

#### Scenario: Fine del coordinamento
- **WHEN** l'ADMIN toglie GOL al Coordinatore, oppure gli cambia il ruolo da Tutor a Call Center
- **THEN** il coordinamento di GOL decade

