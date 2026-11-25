# user-management Specification

## Purpose
TBD - created by archiving change add-user-creation. Update Purpose after archive.
## Requirements
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

### Requirement: Progetti assegnati ai Tutor
L'ADMIN SHALL assegnare e togliere i progetti di un Tutor da NEXUS. Il Tutor MUST vedere in sola lettura i ticket dei
progetti assegnati e poter segnalare solo su quelli, a partire dalla richiesta successiva. Solo i Tutor SHALL avere
progetti assegnati: Call Center e ADMIN vedono già tutti i progetti. Un progetto disattivato MUST NOT essere assegnato,
ma se è già assegnato SHALL poter essere tolto. Ogni modifica SHALL essere registrata nell'audit con i progetti prima
e dopo.

#### Scenario: Assegnazione di un progetto
- **WHEN** l'ADMIN assegna il progetto POLIS a un Tutor
- **THEN** il Tutor vede in sola lettura i ticket di POLIS, può scegliere POLIS in una nuova segnalazione e la
  modifica è nell'audit

#### Scenario: Progetto tolto
- **WHEN** l'ADMIN toglie il progetto POLIS a un Tutor
- **THEN** il Tutor non vede più i ticket di POLIS degli altri Tutor; i ticket che ha segnalato lui restano suoi

#### Scenario: Progetto disattivato
- **WHEN** l'ADMIN prova ad assegnare un progetto disattivato
- **THEN** l'assegnazione è rifiutata; un progetto disattivato già assegnato si può togliere

#### Scenario: Utente che non è Tutor
- **WHEN** l'ADMIN prova ad assegnare progetti a un operatore del Call Center o a un ADMIN
- **THEN** l'assegnazione è rifiutata e l'elenco degli utenti indica «Tutti» per i loro progetti

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor o un operatore prova a cambiare i progetti di un utente
- **THEN** la risposta è `403`

### Requirement: Disattivazione e cambio di ruolo
L'ADMIN SHALL disattivare, riattivare e cambiare il ruolo di un utente da NEXUS; ogni cambio MUST valere sia in NEXUS
sia in Keycloak, e se Keycloak non risponde nulla cambia. Un utente disattivato MUST NOT accedere e le sue sessioni si
chiudono; i suoi ticket restano consultabili. Il nuovo ruolo SHALL valere dal login successivo. Un Tutor con
segnalazioni aperte MUST NOT essere disattivato né cambiare ruolo finché non sono spostate. I ticket in carico a un
operatore disattivato, o che diventa Tutor, MUST tornare liberi nella coda del Call Center. L'ADMIN MUST NOT
disattivare se stesso né cambiarsi il ruolo. Ogni cambio SHALL essere registrato nell'audit.

#### Scenario: Operatore disattivato
- **WHEN** l'ADMIN disattiva un operatore con due ticket in carico, dopo l'avviso
- **THEN** l'operatore non accede più a NEXUS né a Keycloak, i due ticket sono liberi nella coda e la cronologia
  mostra «Tornata in coda»

#### Scenario: Riattivazione
- **WHEN** l'ADMIN riattiva un utente disattivato
- **THEN** l'utente accede di nuovo con la sua password

#### Scenario: Tutor con segnalazioni aperte
- **WHEN** l'ADMIN prova a disattivare un Tutor, o a cambiargli il ruolo, mentre ha segnalazioni aperte
- **THEN** l'operazione è rifiutata e la pagina rimanda allo spostamento delle segnalazioni

#### Scenario: Cambio di ruolo
- **WHEN** l'ADMIN cambia il ruolo di un operatore in ADMIN
- **THEN** il ruolo cambia in NEXUS e in Keycloak, le sessioni dell'utente si chiudono e al login successivo vale il
  nuovo ruolo; un token emesso prima del cambio non riporta indietro il ruolo in NEXUS

#### Scenario: L'ADMIN stesso
- **WHEN** l'ADMIN prova a disattivare il proprio utente o a cambiarsi il ruolo
- **THEN** l'operazione è rifiutata

#### Scenario: Keycloak non raggiungibile
- **WHEN** Keycloak non risponde durante la disattivazione o il cambio di ruolo
- **THEN** la risposta è `503` e in NEXUS non cambia nulla

### Requirement: Registro degli accessi
Ogni accesso a NEXUS, riuscito o fallito, SHALL essere registrato con data, ora, indirizzo IP ed esito; un tentativo
fallito SHALL riportare il motivo. Il registro SHALL essere conservato 12 mesi e poi cancellato automaticamente.
L'ADMIN SHALL consultare il registro di ogni utente e una pagina con i tentativi falliti di tutti, anche con uno
username inesistente; ogni utente SHALL consultare i propri accessi nel profilo. Data e ora MUST essere italiane.

#### Scenario: Accessi di un utente
- **WHEN** l'ADMIN apre gli accessi di un utente che è entrato una volta e ha sbagliato la password una volta
- **THEN** vede due righe dalla più recente: «Riuscito» e «Non riuscito: password sbagliata», con data, ora e IP

#### Scenario: Tentativi falliti di tutti
- **WHEN** qualcuno prova ad accedere con uno username che non esiste
- **THEN** nella pagina «Accessi non riusciti» l'ADMIN vede lo username scritto, «Utente inesistente», data, ora e IP

#### Scenario: I miei accessi
- **WHEN** un utente apre il suo profilo
- **THEN** vede i propri accessi, riusciti e falliti, e non quelli degli altri

#### Scenario: Più di 20 accessi
- **WHEN** gli accessi sono più di 20
- **THEN** se ne vedono 20 e «Mostra accessi più vecchi» carica i successivi

#### Scenario: Registro non disponibile
- **WHEN** Keycloak non risponde
- **THEN** la pagina dice che il registro non è disponibile e propone «Riprova»

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor o un operatore chiede gli accessi di un altro utente o i tentativi falliti
- **THEN** la risposta è `403`

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

