## ADDED Requirements

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
