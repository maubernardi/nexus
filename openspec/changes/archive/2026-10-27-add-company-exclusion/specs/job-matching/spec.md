## ADDED Requirements

### Requirement: Esclusione di un'azienda
Il Call Center e l'ADMIN SHALL poter escludere un'azienda per una segnalazione presa in carico, in lavorazione o
riaperta, indicando un motivo obbligatorio; l'azienda esclusa MUST NOT comparire più tra le mansioni compatibili della
segnalazione. L'esclusione SHALL essere registrata nella cronologia (con il motivo) e nello storico dell'azienda, e le
aziende escluse SHALL essere elencate nel dettaglio con motivo, autore e data.

#### Scenario: Esclusione
- **WHEN** l'operatore esclude Alfa Logistica con il motivo "Non assume nel periodo estivo"
- **THEN** Alfa Logistica non compare più tra le compatibili, l'elenco delle escluse la mostra con il motivo e la
  cronologia registra l'esclusione

#### Scenario: Motivo mancante o azienda già esclusa
- **WHEN** l'operatore non indica il motivo, o l'azienda è già esclusa
- **THEN** il sistema chiede il motivo, oppure risponde `409`

#### Scenario: Stato o ruolo non ammessi
- **WHEN** la segnalazione non è presa in carico, o chi esclude è un Tutor
- **THEN** la risposta è rispettivamente `409` e `403`
