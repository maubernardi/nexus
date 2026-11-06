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

