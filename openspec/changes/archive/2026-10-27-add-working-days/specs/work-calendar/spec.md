## ADDED Requirements

### Requirement: Giorni lavorativi
Il sistema SHALL considerare non lavorativi i sabati, le domeniche, le festività nazionali (1 e 6 gennaio, Pasqua e
Lunedì dell'Angelo, 25 aprile, 1 maggio, 2 giugno, 15 agosto, 1 novembre, 8, 25 e 26 dicembre), il patrono e le
chiusure del calendario unico; il 4 ottobre MUST NOT essere considerato festivo. Il sistema SHALL calcolare l'n-esimo
giorno lavorativo successivo a una data, esclusa la data stessa.

#### Scenario: Fine settimana
- **WHEN** si calcolano 7 giorni lavorativi dopo venerdì 23 ottobre 2026
- **THEN** la scadenza è martedì 3 novembre 2026

#### Scenario: Pasqua e Pasquetta
- **WHEN** si calcolano 3 giorni lavorativi dopo giovedì 2 aprile 2026
- **THEN** il risultato è mercoledì 8 aprile 2026

#### Scenario: Patrono e chiusure
- **WHEN** il 24 giugno è il patrono e dal 10 al 21 agosto c'è una chiusura
- **THEN** quei giorni non sono contati e il calcolo cambia subito se l'ADMIN modifica il calendario

#### Scenario: Prova del calcolo
- **WHEN** l'ADMIN indica nella pagina "Calendario" una data di accettazione
- **THEN** vede il giorno del promemoria (4° lavorativo) e della scadenza (7° lavorativo)
