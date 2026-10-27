## ADDED Requirements

### Requirement: Rifiuto della proposta
Il Tutor della segnalazione SHALL poter rifiutare, con motivo obbligatorio, una proposta non ancora accettata
(`PROPOSTA_AZIENDA`): la segnalazione passa in `RIAPERTO` in carico allo stesso operatore, la mansione torna libera e
l'azienda è esclusa per quella segnalazione, così da non comparire più tra le mansioni compatibili. Il rifiuto SHALL
essere registrato nell'audit, in cronologia (con il motivo) e nello storico dell'azienda, e notificato all'operatore.

#### Scenario: Rifiuto
- **WHEN** il Tutor rifiuta la proposta di Alfa Logistica con il motivo "Orari incompatibili"
- **THEN** la segnalazione è "Riaperto", la mansione è libera, Alfa Logistica non compare tra le compatibili della
  segnalazione e l'operatore riceve la notifica

#### Scenario: Motivo mancante
- **WHEN** il Tutor prova a rifiutare senza motivo
- **THEN** il sistema chiede il motivo

#### Scenario: Dopo l'accettazione o da altri utenti
- **WHEN** il Tutor prova a rifiutare una proposta già accettata, o la rifiuta un collega o un operatore
- **THEN** la risposta è rispettivamente `409` e `403`
