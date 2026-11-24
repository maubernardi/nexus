## ADDED Requirements

### Requirement: Correzione dei dati dell'utente
L'ADMIN SHALL correggere da NEXUS nome, cognome, username, email e telefono di un utente, attivo o disattivato. Ogni
correzione MUST valere sia in NEXUS sia in Keycloak; se Keycloak non risponde o rifiuta, in NEXUS non cambia nulla.
Username ed email MUST restare unici, senza distinguere maiuscole e minuscole, con le stesse regole della creazione.
Lo username SHALL potersi correggere solo finché l'utente non ha mai impostato la password, perché l'audit registra con
lo username l'autore delle azioni; dopo, MUST essere rifiutato. Se cambia l'email di un utente che non ha ancora
impostato la password, NEXUS SHALL inviargli di nuovo l'invito al nuovo indirizzo, e il collegamento inviato al vecchio
MUST NOT valere più. Se cambia l'email di un utente che ha già la password, il vecchio indirizzo SHALL ricevere
l'avviso del cambio; una sua richiesta di cambio dell'email ancora in attesa MUST essere annullata. Ogni correzione
SHALL essere registrata nell'audit con i campi cambiati: lo username con i valori prima e dopo, gli altri dati
personali senza i valori.

#### Scenario: Username sbagliato di un utente mai entrato
- **WHEN** l'ADMIN corregge lo username `mario.rosi` in `mario.rossi` di un utente che non ha ancora impostato la
  password
- **THEN** lo username cambia in NEXUS e in Keycloak, l'invito già ricevuto resta valido, l'utente entra con
  `mario.rossi` e la correzione è nell'audit con i due username

#### Scenario: Utente già entrato
- **WHEN** l'ADMIN apre i dati di un utente che ha già impostato la password
- **THEN** lo username è in sola lettura con il motivo; nome, cognome, email e telefono si possono correggere, e una
  richiesta che cambia lo username è rifiutata

#### Scenario: Email di un utente mai entrato
- **WHEN** l'ADMIN corregge l'email di un utente che non ha ancora impostato la password
- **THEN** l'invito parte verso il nuovo indirizzo e il collegamento inviato al vecchio indirizzo non funziona più

#### Scenario: Email di un utente già entrato
- **WHEN** l'ADMIN corregge l'email di un utente che ha già impostato la password
- **THEN** l'email cambia in NEXUS e in Keycloak, il vecchio indirizzo riceve l'avviso del cambio e nessun invito parte

#### Scenario: Dati già usati
- **WHEN** il nuovo username o la nuova email sono già usati da un altro utente, in NEXUS o in Keycloak
- **THEN** il campo segnala il problema e non cambia nulla

#### Scenario: Keycloak non raggiungibile
- **WHEN** Keycloak non risponde durante la correzione
- **THEN** la risposta è `503` e in NEXUS non cambia nulla

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor o un operatore chiede di leggere o correggere i dati di un utente
- **THEN** la risposta è `403`
