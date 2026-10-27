## ADDED Requirements

### Requirement: Appuntamento fissato
Il Tutor della segnalazione SHALL poter registrare, previa conferma e con una nota facoltativa, che l'appuntamento con
l'azienda è fissato, per una segnalazione in `PROPOSTA_ACCOLTA`: la segnalazione passa in `APPUNTAMENTO` e il timer si
ferma. L'evento SHALL essere registrato nell'audit (con i valori del timer), nella cronologia e nello storico
dell'azienda, e notificato all'operatore che ha in carico la segnalazione. Altri utenti MUST NOT poterlo registrare.

#### Scenario: Appuntamento registrato
- **WHEN** il Tutor registra l'appuntamento con la nota "Giovedì alle 10"
- **THEN** lo stato è "Appuntamento", la scadenza del timer non c'è più, la cronologia mostra "Appuntamento fissato" con
  la nota e l'operatore riceve la notifica

#### Scenario: Prima dell'accettazione
- **WHEN** il Tutor prova a registrare l'appuntamento su una proposta non ancora accettata
- **THEN** il sistema risponde `409`

#### Scenario: Collega o Call Center
- **WHEN** un collega di progetto o un operatore prova a registrare l'appuntamento
- **THEN** la risposta è `403`
