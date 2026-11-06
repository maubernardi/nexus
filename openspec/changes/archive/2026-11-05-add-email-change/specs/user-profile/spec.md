## ADDED Requirements

### Requirement: Pagina del profilo
Ogni utente autenticato (Tutor, Call Center, ADMIN) SHALL avere una pagina «Il mio profilo», raggiungibile dal proprio
nome nell'intestazione e nel menu mobile, con nome, ruolo e indirizzo email registrato in NEXUS.

#### Scenario: Apertura del profilo
- **WHEN** l'utente attiva il proprio nome nell'intestazione
- **THEN** si apre «Il mio profilo» con nome, ruolo e indirizzo email attuale

### Requirement: Cambio dell'indirizzo confermato con codice
Il cambio del proprio indirizzo email SHALL avvenire in due passi: alla richiesta NEXUS MUST inviare al nuovo
indirizzo un codice di 6 cifre valido 30 minuti; solo con il codice corretto l'indirizzo cambia, in NEXUS e
nell'identity provider. Il codice MUST essere salvato solo in forma di hash e MUST NOT comparire nei log in
produzione. Il nuovo indirizzo MUST essere valido, diverso dall'attuale e non usato da un altro utente.

#### Scenario: Richiesta del codice
- **WHEN** l'utente scrive un nuovo indirizzo valido e lo invia
- **THEN** il nuovo indirizzo riceve un'email con il codice di 6 cifre e la pagina chiede il codice, indicando
  l'indirizzo e la scadenza

#### Scenario: Conferma
- **WHEN** l'utente scrive il codice corretto entro 30 minuti
- **THEN** l'indirizzo cambia in NEXUS e in Keycloak, le notifiche partono verso il nuovo indirizzo e la pagina lo
  mostra

#### Scenario: Indirizzo già usato
- **WHEN** l'utente chiede di passare a un indirizzo già usato da un altro utente
- **THEN** la richiesta è rifiutata con un messaggio sul campo e nessun codice viene inviato

#### Scenario: Codice sbagliato
- **WHEN** l'utente scrive un codice sbagliato
- **THEN** l'indirizzo non cambia e il messaggio indica quanti tentativi restano

#### Scenario: Troppi tentativi o codice scaduto
- **WHEN** l'utente ha sbagliato 5 volte, oppure sono passati più di 30 minuti
- **THEN** il codice non vale più, anche se corretto, e l'utente deve chiederne uno nuovo

#### Scenario: Nuovo codice
- **WHEN** l'utente chiede un nuovo codice
- **THEN** il precedente non vale più; un altro codice si può chiedere solo dopo 60 secondi dall'ultimo invio

#### Scenario: Annullamento
- **WHEN** l'utente annulla la richiesta in corso
- **THEN** il codice non vale più e l'indirizzo resta quello attuale

#### Scenario: Invio non riuscito
- **WHEN** l'email col codice non si può inviare (server di posta irraggiungibile o non configurato)
- **THEN** la richiesta non si salva e l'utente vede che il cambio non è disponibile al momento

#### Scenario: Identity provider non raggiungibile
- **WHEN** alla conferma Keycloak rifiuta l'aggiornamento o non risponde
- **THEN** l'indirizzo non cambia nemmeno in NEXUS e l'utente vede l'errore

### Requirement: Avviso al vecchio indirizzo e audit
Dopo un cambio confermato NEXUS SHALL inviare al vecchio indirizzo un avviso del cambio, con l'invito a contattare
l'amministratore se non l'ha chiesto l'utente, e SHALL registrare il cambio nell'audit dell'utente senza i valori
degli indirizzi. Un invio fallito dell'avviso MUST NOT annullare il cambio.

#### Scenario: Avviso e audit
- **WHEN** l'utente conferma il nuovo indirizzo
- **THEN** il vecchio indirizzo riceve l'avviso e l'audit registra il cambio dell'email (autore, istante), senza
  gli indirizzi
