## ADDED Requirements

### Requirement: Primo ADMIN della produzione
Se in NEXUS non c'è nessun ADMIN e nella configurazione del server ci sono username, nome, cognome ed email dell'ADMIN
iniziale, l'applicazione SHALL crearlo come farebbe un ADMIN da «Utenti»: account in Keycloak con il ruolo ADMIN,
utente in NEXUS, audit con l'autore di sistema e invito per email. Se Keycloak non risponde, SHALL riprovare finché
non riesce. Con un ADMIN già presente, o con dati mancanti o non validi, MUST NOT creare nessuno.

#### Scenario: Produzione senza utenti
- **WHEN** il backend parte dopo il passaggio in produzione, senza utenti e con i dati dell'ADMIN nel `.env`
- **THEN** entro un paio di minuti l'ADMIN esiste in Keycloak e in NEXUS e riceve l'email per impostare la password

#### Scenario: ADMIN già presente
- **WHEN** in NEXUS c'è già un ADMIN, attivo o no
- **THEN** non viene creato nessuno

#### Scenario: Keycloak non ancora pronto
- **WHEN** alla prima prova Keycloak non risponde
- **THEN** l'ADMIN non viene creato, e la prova successiva lo crea

#### Scenario: Dati non validi
- **WHEN** l'email dell'ADMIN nel `.env` non è un indirizzo valido
- **THEN** non viene creato nessuno e il log del backend dice quale dato correggere
