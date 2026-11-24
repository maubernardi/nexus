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

### Requirement: Controllo della disponibilità
Un controllo esterno SHALL verificare ogni 10 minuti, dalle 8 alle 20 ora italiana, che la PWA, Keycloak e il backend
della demo rispondano e che i certificati non scadano entro 14 giorni. Un controllo fallito dopo 3 tentativi SHALL
avvisare il PO per email. Il controllo MUST NOT usare segreti.

#### Scenario: Demo online
- **WHEN** il controllo gira alle 10:00 e sito, Keycloak e API rispondono come previsto
- **THEN** il controllo riesce e nessuno viene avvisato

#### Scenario: Backend giù
- **WHEN** l'API risponde `502` per tre tentativi di fila
- **THEN** il controllo fallisce e il PO riceve un'email da GitHub

#### Scenario: Fuori orario
- **WHEN** il controllo pianificato parte alle 21:30 ora italiana
- **THEN** non controlla nulla e non avvisa

### Requirement: Revisione di sicurezza
Prima del passaggio in produzione SHALL esserci una revisione di sicurezza secondo OWASP ASVS livello 1, con l'esito di
ogni requisito applicabile, le lacune e le azioni proposte; SHALL essere aggiornata quando cambiano parti esaminate
(caricamento di file, produzione).

#### Scenario: Esito documentato
- **WHEN** il PO apre `docs/security/asvs-l1.md`
- **THEN** trova per ogni capitolo i requisiti con esito ed evidenza, e le lacune con chi e quando le chiude

### Requirement: Dipendenze aggiornate
Le dipendenze del frontend, del backend, delle immagini e della CI SHALL essere all'ultima versione stabile; Java e Node
MUST essere all'ultima LTS. Gli aggiornamenti SHALL arrivare con le PR di Dependabot, una a settimana per ecosistema per
le versioni minori e una per ogni versione principale, e passare dalla CI prima del merge del PO. Le vulnerabilità
segnalate SHALL essere chiuse aggiornando; un avviso senza correzione esistente MAY essere archiviato con il motivo e
l'ok del PO. Le immagini usate dai test, dal backup, dal ripristino di prova e dallo script di aggiornamento MUST essere
le stesse della produzione. Il volume dei dati di PostgreSQL MUST avere nel nome la versione principale dell'immagine.

#### Scenario: Aggiornamento settimanale
- **WHEN** esce una versione minore di una dipendenza del frontend
- **THEN** il lunedì successivo Dependabot apre una PR con tutte le minori del frontend, che il PO unisce dopo la CI verde

#### Scenario: Versione principale
- **WHEN** esce una nuova versione principale di una dipendenza
- **THEN** Dependabot apre una PR solo per quella; per Java e per i tipi di Node non la apre, perché seguono la LTS

#### Scenario: Immagini dei test diverse dalla produzione
- **WHEN** l'immagine di PostgreSQL o di Keycloak della produzione cambia e quella dei test no
- **THEN** la CI fallisce indicando il file da aggiornare

#### Scenario: Nuova versione principale di PostgreSQL senza volume nuovo
- **WHEN** una PR porta PostgreSQL a una nuova versione principale e lascia il volume con il nome della precedente
- **THEN** la CI fallisce e rimanda alla procedura di aggiornamento del README

### Requirement: Backup notturno cifrato fuori dal server
Ogni notte alle 3:30, ora italiana, il server SHALL salvare i database di NEXUS e di Keycloak in un unico archivio.
L'archivio SHALL essere cifrato sul server con la chiave pubblica del PO e caricato su IONOS Object Storage, con i dati
in Germania. La chiave che decifra MUST NOT stare sul server, nel repository o nelle immagini. Le chiavi di IONOS
SHALL stare solo nel `.env` del server. Dopo il caricamento il servizio SHALL controllare che l'archivio sia arrivato
intero, e SHALL registrare l'esito: data, dimensione e nome dell'ultimo backup riuscito, oppure il passo fallito.
L'esito e i log MUST NOT contenere dati personali.

#### Scenario: Backup della notte
- **WHEN** alle 3:30 il database risponde e IONOS è raggiungibile
- **THEN** su IONOS c'è un nuovo archivio cifrato con i due database, e l'esito riporta il backup come riuscito

#### Scenario: Archivio senza la chiave del PO
- **WHEN** qualcuno scarica un archivio da IONOS senza la chiave privata del PO
- **THEN** non può leggerne il contenuto

#### Scenario: Backup non configurato
- **WHEN** nel `.env` mancano le chiavi di IONOS o la chiave pubblica
- **THEN** il servizio non si ferma e non blocca il deploy; l'esito dice «non configurato» e indica la variabile
  mancante

#### Scenario: Caricamento fallito
- **WHEN** IONOS rifiuta il caricamento
- **THEN** l'esito indica il passo fallito, l'ultimo backup riuscito resta quello di prima e sul server non rimane
  nessuna copia dei dati

### Requirement: Conservazione e protezione dei backup
I backup SHALL restare su IONOS per 30 giorni, poi SHALL cancellarsi da soli con una regola del contenitore impostata
dal servizio. Il contenitore SHALL avere il blocco degli oggetti in modalità *governance* per 30 giorni: per 30 giorni
una richiesta normale MUST NOT poter cancellare o sovrascrivere un backup, nemmeno con le chiavi del server. Solo chi
ha il permesso di scavalcare il blocco e lo chiede esplicitamente MAY farlo: il PO, dalla console di IONOS.

#### Scenario: Backup più vecchio di 30 giorni
- **WHEN** un backup ha più di 30 giorni
- **THEN** IONOS lo cancella da solo, senza interventi

#### Scenario: Tentativo di cancellare un backup recente
- **WHEN** con le chiavi del server si tenta di cancellare definitivamente un backup di 10 giorni prima
- **THEN** IONOS rifiuta e il backup resta

#### Scenario: Cancellazione voluta dal PO
- **WHEN** il PO, dalla console di IONOS, sceglie di scavalcare il blocco per cancellare un backup
- **THEN** IONOS lo permette

#### Scenario: Blocco assente
- **WHEN** il contenitore è stato creato senza il blocco degli oggetti
- **THEN** il backup riesce, ma l'esito lo segnala come non protetto

### Requirement: Controllo del mattino dei backup
Ogni mattina alle 9, ora italiana, anche con l'ora legale, un workflow SHALL leggere l'esito dal server con la
chiave di deploy. Se manca un backup riuscito nelle ultime 26 ore, il controllo SHALL fallire e GitHub SHALL avvisare
il PO per email. Dalle Actions il PO SHALL poter leggere lo stato dei backup e i log del servizio, e avviare un backup
subito.

#### Scenario: Backup della notte riuscito
- **WHEN** il controllo gira alle 9 e l'ultimo backup riuscito è delle 3:30
- **THEN** il controllo riesce e nessuno viene avvisato

#### Scenario: Backup mancante
- **WHEN** l'ultimo backup riuscito ha più di 26 ore, o non ce n'è nessuno
- **THEN** il controllo fallisce e il PO riceve un'email da GitHub

#### Scenario: Ora legale
- **WHEN** in estate il cron di GitHub parte alle 7:00 UTC e in inverno alle 8:00 UTC
- **THEN** il controllo gira una sola volta al giorno, alle 9 ora italiana

#### Scenario: Backup su richiesta
- **WHEN** il PO avvia dalle Actions un backup subito
- **THEN** il backup parte, l'esito compare nel log del workflow, e un backup notturno già in corso non viene
  duplicato

### Requirement: Ripristino provato
Il ripristino SHALL essere documentato nel README e provato: scaricare l'archivio, decifrarlo con la chiave del PO,
controllare le impronte, ripristinare i due database e ritrovare i dati. Uno script SHALL fare la prova sul PC del PO
in un PostgreSQL temporaneo. La CI SHALL provare a ogni PR il percorso completo, dal backup al ripristino, su un
servizio S3 di prova con una chiave generata al momento.

#### Scenario: Ripristino di prova
- **WHEN** il PO lancia lo script con un archivio scaricato e la sua chiave privata
- **THEN** i due database sono ripristinati in un PostgreSQL temporaneo e lo script mostra il numero di righe delle
  tabelle principali e l'ultima migrazione

#### Scenario: Archivio alterato
- **WHEN** l'archivio è stato modificato dopo il backup, o la chiave non è quella giusta
- **THEN** lo script si ferma con un errore chiaro e non ripristina nulla

#### Scenario: Prova nella CI
- **WHEN** una PR cambia il servizio di backup
- **THEN** la CI fa un backup di un database di prova, lo carica, lo ripristina e confronta le righe, e fallisce se
  non coincidono

### Requirement: Aggiornamento della versione principale di PostgreSQL
Il passaggio a una nuova versione principale di PostgreSQL SHALL spostare i dati con un backup e un ripristino, senza
perdite. I dati della nuova versione MUST stare in un volume nuovo, con la versione principale nel nome. Il volume della
versione precedente MUST restare intatto finché il PO non lo toglie. Uno script, lanciato dal PO con sudo prima del
deploy, SHALL:
1. fare un backup su IONOS;
2. copiare i due database;
3. ripristinarli nella nuova versione, sul volume nuovo;
4. confrontare righe per tabella, trigger, ultima migrazione e utenti di Keycloak.

Dopo il deploy, lo stesso script SHALL rifare il confronto e un backup. Nel terminale MUST comparire solo numeri e
codici, nessun dato personale.

#### Scenario: Aggiornamento riuscito
- **WHEN** il PO lancia lo script sulla versione precedente, conferma, e poi si fa il deploy della nuova
- **THEN** la nuova versione parte sul volume già ripristinato, con le stesse righe, gli stessi trigger e la stessa
  ultima migrazione, e backend e Keycloak non partono mai su un database vuoto

#### Scenario: Ripristino con numeri diversi
- **WHEN** dopo il ripristino le righe, i trigger o gli utenti di Keycloak non coincidono con l'origine
- **THEN** lo script si ferma, toglie il volume nuovo e spiega come riavviare il sito sulla versione precedente, che non
  è cambiata

#### Scenario: Ritorno indietro
- **WHEN** dopo l'aggiornamento si fa il deploy del commit precedente
- **THEN** la versione precedente riparte dal suo volume con i dati di prima dell'aggiornamento

#### Scenario: Deploy durante lo script
- **WHEN** parte un deploy mentre lo script copia o ripristina i dati
- **THEN** il deploy si ferma con «un altro deploy è in corso» e non avvia niente

#### Scenario: Pulizia
- **WHEN** il PO lancia la pulizia con la nuova versione in esecuzione sul volume nuovo
- **THEN** dopo la sua conferma il volume della versione precedente è cancellato; se la nuova versione non è quella in
  esecuzione, la pulizia si rifiuta

