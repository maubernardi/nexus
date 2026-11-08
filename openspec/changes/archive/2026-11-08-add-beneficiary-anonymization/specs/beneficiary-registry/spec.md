## ADDED Requirements

### Requirement: Conservazione e anonimizzazione
Il sistema SHALL segnare ogni notte «Da anonimizzare» i beneficiari senza attività (scheda, segnalazioni, schede dei
tirocini) da più del periodo di conservazione, per ora 2 anni; una nuova attività SHALL togliere il segno. Il Tutor
proprietario e l'ADMIN SHALL vedere evidenziati e filtrare i beneficiari da anonimizzare e decidere: anonimizzarli, dopo
una conferma, oppure tenerne i dati per sempre con un motivo obbligatorio. L'anonimizzazione MUST lasciare di nome e
cognome solo le iniziali e cancellare CV, vincoli e, dalle schede dei tirocini, codice fiscale, data e luogo di nascita,
residenza e note; i dati statistici MUST restare. Ogni passaggio SHALL essere registrato nell'audit senza valori
personali. Un beneficiario anonimizzato MUST NOT essere modificato né segnalato.

#### Scenario: Segno notturno
- **WHEN** un beneficiario non ha attività da più di 2 anni
- **THEN** il controllo della notte lo segna «Da anonimizzare», senza che il segno conti come attività

#### Scenario: Nuova attività
- **WHEN** una segnalazione del beneficiario segnato si muove o il Tutor ne modifica la scheda
- **THEN** il beneficiario non è più da anonimizzare

#### Scenario: Anonimizzazione confermata
- **WHEN** il Tutor conferma l'anonimizzazione di Mario Rossi
- **THEN** restano «M» e «R», l'anno di nascita, la zona e gli altri dati statistici; vincoli e dati identificativi della
  scheda del tirocinio sono cancellati e l'audit non contiene i valori

#### Scenario: Dati tenuti
- **WHEN** l'ADMIN sceglie di tenere i dati senza scrivere il motivo
- **THEN** la richiesta è rifiutata; con il motivo i dati si tengono e il beneficiario non viene più segnato

#### Scenario: Chi decide
- **WHEN** un altro Tutor o un operatore prova a decidere su un beneficiario, o qualcuno decide su un beneficiario non
  da anonimizzare
- **THEN** la richiesta è rifiutata
