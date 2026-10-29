## ADDED Requirements

### Requirement: Legale rappresentante dell'azienda
Il Call Center e l'ADMIN SHALL poter registrare e correggere nella scheda dell'azienda il legale rappresentante, che
può non coincidere con il referente: nome e cognome (obbligatori), codice fiscale, data e luogo di nascita, residenza.
Il codice fiscale MUST essere valido, carattere di controllo compreso. Sono dati personali: l'audit SHALL registrare
quali campi sono cambiati, MUST NOT registrarne i valori.

#### Scenario: Prima registrazione
- **WHEN** l'operatore indica il legale rappresentante con un codice fiscale scritto in minuscolo
- **THEN** i dati sono salvati con il codice fiscale in maiuscolo e la scheda li mostra

#### Scenario: Codice fiscale non valido
- **WHEN** il carattere di controllo del codice fiscale non torna
- **THEN** il campo segnala "Codice fiscale non valido" e nulla è salvato

#### Scenario: Audit senza valori
- **WHEN** l'operatore corregge la residenza
- **THEN** l'audit registra che la residenza è cambiata, senza il vecchio né il nuovo indirizzo

### Requirement: Codici ATECO dell'azienda
Il Call Center e l'ADMIN SHALL poter aggiungere e togliere i codici ATECO di un'azienda, uno o più, con una
descrizione facoltativa. Un'azienda MUST NOT avere due volte lo stesso codice; il formato SHALL essere `NN`, `NN.NN` o
`NN.NN.NN`.

#### Scenario: Aggiunta
- **WHEN** l'operatore aggiunge 56.10.11 con la descrizione
- **THEN** il codice compare nell'elenco dell'azienda e l'aggiunta è nell'audit

#### Scenario: Codice già presente
- **WHEN** l'operatore aggiunge un codice che l'azienda ha già
- **THEN** il campo lo segnala
