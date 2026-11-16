## ADDED Requirements

### Requirement: Motivi di chiusura del tirocinio
L'ADMIN SHALL gestire i motivi di chiusura del tirocinio come gli altri cataloghi: aggiunta, rinomina, disattivazione
e riattivazione, con l'audit. Le voci iniziali MUST essere «Chiusura regolare», «Interruzione anticipata per volontà
dell'azienda» e «Interruzione anticipata per volontà del destinatario». «Chiusura regolare» è il motivo automatico dei
tirocini chiusi alla data prevista o dopo: MUST NOT essere disattivabile. Le altre voci attive sono le interruzioni che
il Tutor può scegliere.

#### Scenario: Nuovo motivo
- **WHEN** l'ADMIN aggiunge «Interruzione anticipata per assunzione altrove»
- **THEN** la voce è attiva e il Tutor la trova tra i motivi dell'interruzione

#### Scenario: Chiusura regolare non disattivabile
- **WHEN** l'ADMIN prova a disattivare «Chiusura regolare»
- **THEN** l'operazione è rifiutata con la spiegazione e la voce resta attiva
