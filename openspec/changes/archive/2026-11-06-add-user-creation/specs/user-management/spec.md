## ADDED Requirements

### Requirement: Creazione degli utenti
L'ADMIN SHALL creare un utente da NEXUS indicando nome, cognome, username, email, telefono facoltativo e ruolo.
L'utente MUST esistere in Keycloak e in NEXUS con lo stesso identificativo e il ruolo scelto. Username ed email MUST
essere unici, senza distinguere maiuscole e minuscole. Se l'account Keycloak viene creato ma NEXUS non salva l'utente,
l'account MUST essere cancellato. La creazione SHALL essere registrata nell'audit senza i valori dei dati personali.

#### Scenario: Nuovo utente
- **WHEN** l'ADMIN crea «Mario Rossi», username `mario.rossi`, ruolo Call Center
- **THEN** l'utente esiste in NEXUS e in Keycloak con lo stesso identificativo e il ruolo Call Center, compare
  nell'elenco degli utenti e la creazione è nell'audit

#### Scenario: Username proposto
- **WHEN** l'ADMIN scrive nome e cognome e lo username è vuoto
- **THEN** lo username proposto è `nome.cognome`, senza accenti né spazi, e si può cambiare

#### Scenario: Dati già usati
- **WHEN** lo username o l'email sono già usati da un altro utente, in NEXUS o in Keycloak
- **THEN** il campo segnala il problema e nessun utente viene creato

#### Scenario: Salvataggio in NEXUS non riuscito
- **WHEN** Keycloak crea l'account ma NEXUS non riesce a salvare l'utente
- **THEN** l'account Keycloak viene cancellato e l'ADMIN vede l'errore

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor o un operatore chiede l'elenco o la creazione degli utenti
- **THEN** la risposta è `403`

### Requirement: Email per impostare la password
Alla creazione l'utente SHALL ricevere da Keycloak un'email con il collegamento per impostare la password, valido
3 giorni, che dopo porta all'app. Se l'email non parte, l'utente MUST restare creato e l'ADMIN SHALL poterla inviare
di nuovo dall'elenco degli utenti. Ogni invito inviato SHALL essere registrato nell'audit.

#### Scenario: Invito inviato
- **WHEN** l'ADMIN crea un utente
- **THEN** l'utente riceve l'email per impostare la password e l'elenco conferma l'invio

#### Scenario: Invito non partito
- **WHEN** l'email non parte (server di posta di Keycloak non configurato o irraggiungibile)
- **THEN** l'utente è creato, l'elenco avvisa che l'invito non è partito e offre «Invia di nuovo l'invito»
