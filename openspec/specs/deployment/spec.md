# deployment Specification

## Purpose
Definisce come NEXUS viene pubblicata online: comunicazioni cifrate, superficie esposta minima, segreti fuori dal
codice, deploy riproducibile e controllato, e le regole dell'ambiente demo.
## Requirements
### Requirement: Solo HTTPS
L'applicazione e l'identity provider SHALL essere raggiungibili solo tramite HTTPS con certificato valido; le richieste
HTTP MUST essere reindirizzate a HTTPS. Le risposte SHALL includere HSTS, una Content Security Policy restrittiva e il
divieto di essere incorporate in frame di altri siti.

#### Scenario: Accesso in HTTP
- **WHEN** un browser apre `http://portalenexus.it`
- **THEN** viene reindirizzato permanentemente a `https://portalenexus.it`

#### Scenario: Dominio www
- **WHEN** un browser apre `https://www.portalenexus.it`
- **THEN** viene reindirizzato a `https://portalenexus.it`

#### Scenario: Header di sicurezza
- **WHEN** si scarica la pagina principale
- **THEN** la risposta contiene `Strict-Transport-Security`, `Content-Security-Policy` e `X-Content-Type-Options: nosniff`

### Requirement: Superficie esposta minima
Dal server SHALL essere raggiungibili da Internet solo SSH e le porte web 80/443. Database, backend e identity provider
MUST NOT essere raggiungibili direttamente. Le API SHALL essere servite sotto `/api` dello stesso dominio dell'app e gli
endpoint tecnici del backend (actuator, documentazione OpenAPI) MUST NOT essere esposti. La console di amministrazione
dell'identity provider MUST NOT essere esposta su Internet.

#### Scenario: Database non raggiungibile
- **WHEN** da Internet si tenta una connessione alla porta di PostgreSQL del server
- **THEN** la connessione non riesce

#### Scenario: Console di amministrazione
- **WHEN** da Internet si apre `https://auth.portalenexus.it/admin/`
- **THEN** l'accesso è negato

#### Scenario: Endpoint tecnici
- **WHEN** da Internet si apre `https://portalenexus.it/actuator/health` o `https://portalenexus.it/v3/api-docs`
- **THEN** la risorsa non è disponibile

### Requirement: Segreti fuori dal codice
Password del database, credenziali di amministrazione e password degli utenti demo SHALL essere generate sul server e
conservate solo lì, leggibili solo dall'utente di deploy; MUST NOT comparire nel repository, nelle immagini o nei log
della pipeline.

#### Scenario: Immagine pubblicata
- **WHEN** si ispeziona un'immagine pubblicata su GitHub Container Registry
- **THEN** non contiene password né chiavi

### Requirement: Deploy su comando e riproducibile
Il deploy SHALL partire solo su azione esplicita del committente, SHALL installare esattamente le immagini costruite
da un commit identificato del repository e SHALL concludersi con una verifica che l'app e l'identity provider
rispondano. La credenziale usata dalla pipeline per accedere al server MUST permettere solo l'esecuzione del deploy.

#### Scenario: Deploy di un commit
- **WHEN** il committente avvia il deploy su un commit di `main`
- **THEN** il server esegue le immagini taggate con quel commit e la pipeline termina con successo solo se l'app risponde

#### Scenario: Uso improprio della chiave di deploy
- **WHEN** qualcuno usa la chiave di deploy per aprire una shell o eseguire un comando arbitrario sul server
- **THEN** il server rifiuta ed esegue solo lo script di deploy

### Requirement: Privilegi del database
L'applicazione SHALL accedere al database con un utente privo di permessi sullo schema (niente DDL); le migrazioni
SHALL essere eseguite da un utente distinto, proprietario dello schema.

#### Scenario: Tentativo di rimuovere il trigger dell'audit trail
- **WHEN** l'utente applicativo tenta di eliminare il trigger di immutabilità dell'audit trail
- **THEN** il database rifiuta l'operazione

### Requirement: Ambiente demo
L'ambiente demo SHALL contenere solo dati fittizi (seed dimostrativo) e utenti demo con password robuste generate
al momento della preparazione del server; le password di sviluppo MUST NOT essere usate online.

#### Scenario: Password di sviluppo
- **WHEN** si tenta l'accesso a `https://auth.portalenexus.it` come `tutor1` con la password di sviluppo
- **THEN** l'accesso è rifiutato

### Requirement: Controllo delle migrazioni nelle PR
La CI SHALL rifiutare una PR che aggiunge una migrazione Flyway con numero non superiore all'ultima del branch di
destinazione o duplicato, oppure che modifica, rinomina o cancella una migrazione già presente sul branch di
destinazione, indicando nel messaggio il problema e, per la numerazione, il numero da usare.

#### Scenario: PR rimasta indietro
- **WHEN** una PR aggiunge `V4__…` e sul branch di destinazione esistono già V4 e V5
- **THEN** il controllo fallisce e suggerisce di rinominarla in `V6__…`

#### Scenario: Migrazione già unita modificata
- **WHEN** una PR modifica una migrazione presente sul branch di destinazione
- **THEN** il controllo fallisce e invita a creare una nuova migrazione

#### Scenario: PR senza migrazioni
- **WHEN** una PR non tocca le migrazioni
- **THEN** il controllo passa

