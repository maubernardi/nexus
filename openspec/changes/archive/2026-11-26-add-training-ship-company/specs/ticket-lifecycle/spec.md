## ADDED Requirements

### Requirement: Proposta di nave scuola nel report di restituzione
Il report di restituzione SHALL avere la domanda facoltativa «L'azienda è adatta come nave scuola?». Se il Tutor
risponde sì, con l'invio:
- l'operatore della segnalazione SHALL ricevere l'avviso della proposta;
- il diario dell'azienda SHALL avere una nota con la proposta, la segnalazione e la data.

L'azienda MUST NOT diventare nave scuola da sola: decide l'operatore. La risposta SHALL entrare nell'audit della
chiusura e comparire nel report inviato.

#### Scenario: Proposta
- **WHEN** il Tutor invia il report rispondendo sì alla nave scuola
- **THEN** l'operatore della segnalazione riceve l'avviso, il diario dell'azienda ha la nota della proposta e
  l'azienda non è ancora una nave scuola

#### Scenario: Nessuna proposta
- **WHEN** il Tutor invia il report senza rispondere, o rispondendo no
- **THEN** non parte nessun avviso e il diario non cambia
