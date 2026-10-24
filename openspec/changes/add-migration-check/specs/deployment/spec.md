## ADDED Requirements

### Requirement: Controllo delle migrazioni nelle PR
La CI SHALL rifiutare una PR che aggiunge una migrazione Flyway con numero non superiore all'ultima del branch di
destinazione o duplicato, oppure che modifica, rinomina o cancella una migrazione già presente sul branch di
destinazione, indicando nel messaggio il problema e, per la numerazione, il numero da usare.

#### Scenario: PR rimasta indietro
- **WHEN** una PR aggiunge `V4__…` e sul branch di destinazione esistono già V4 e V5
- **THEN** il controllo fallisce e suggerisce di rinominarla in `V6__…`

#### Scenario: Migrazione già unita modificata
- **WHEN** una PR modifica una migrazione presente sul branch di destinazione
- **THEN** il controllo fallisce e invita a creare una nuova migrazione

#### Scenario: PR senza migrazioni
- **WHEN** una PR non tocca le migrazioni
- **THEN** il controllo passa
