## ADDED Requirements

### Requirement: Gestione degli annunci
Solo il Call Center e l'ADMIN SHALL vedere, modificare, pubblicare e archiviare gli annunci della bacheca, divisi in
"Bozze", "Pubblicati" e "Archiviati". Ogni azione SHALL essere registrata nell'audit con autore, istante e valori prima
e dopo.

#### Scenario: Elenco per stato
- **WHEN** un operatore apre la bacheca
- **THEN** vede le bozze con numero (#N), titolo, azienda, mansione, zona e progetto, e può passare ai pubblicati e
  agli archiviati

#### Scenario: Ruolo non abilitato
- **WHEN** un Tutor chiede la gestione degli annunci all'API
- **THEN** la risposta è `403`

### Requirement: Modifica di un annuncio
L'operatore SHALL poter modificare titolo, fascia d'età, ore settimanali, durata e note di una bozza o di un annuncio
pubblicato. Il progetto riservatario SHALL essere modificabile solo nella bozza. Un annuncio archiviato MUST NOT essere
modificato. Le modifiche MUST usare il blocco ottimistico.

#### Scenario: Modifica di una bozza
- **WHEN** l'operatore cambia titolo e progetto di una bozza
- **THEN** l'annuncio è salvato e l'audit registra i campi cambiati

#### Scenario: Progetto di un annuncio pubblicato
- **WHEN** l'operatore prova a cambiare il progetto di un annuncio pubblicato
- **THEN** la risposta è `409`: si rende pubblico solo con lo sblocco dopo 7 giorni

#### Scenario: Fascia d'età incoerente
- **WHEN** l'età minima è maggiore della massima
- **THEN** il modulo segnala l'errore sul campo dell'età massima

### Requirement: Pubblicazione e archiviazione
L'operatore SHALL poter pubblicare una bozza, registrandone la data, solo se la mansione è libera e a disposizione e
l'azienda è attiva. L'operatore SHALL poter archiviare una bozza o un annuncio pubblicato.

#### Scenario: Pubblicazione
- **WHEN** l'operatore pubblica una bozza e conferma
- **THEN** l'annuncio passa tra i pubblicati con la data di pubblicazione

#### Scenario: Mansione non proponibile
- **WHEN** la mansione dell'annuncio è bloccata da una segnalazione
- **THEN** la pubblicazione è rifiutata con il motivo

#### Scenario: Archiviazione
- **WHEN** l'operatore archivia un annuncio e conferma
- **THEN** l'annuncio passa tra gli archiviati, in sola lettura
