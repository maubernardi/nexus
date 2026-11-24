## ADDED Requirements

### Requirement: Passaggio dalla demo alla produzione
Il passaggio SHALL avvenire sullo stesso server con uno script lanciato dal PO, che chiede conferma prima di toccare i
dati e parte da un backup della demo su IONOS. La produzione SHALL tenere configurazione e cataloghi della demo e le
impostazioni di Keycloak. SHALL togliere segnalazioni, beneficiari, utenti (in NEXUS e in Keycloak, tranne l'account
tecnico del backend) e aziende della demo, con la loro storia. Le aziende SHALL arrivare dal database attuale.
La pulizia del database SHALL avvenire in una sola transazione. Le regole di sola aggiunta MAY essere sospese solo
dentro quella transazione e MUST tornare attive alla fine. Dopo il passaggio il backend MUST NOT caricare dati
dimostrativi, e Keycloak MUST NOT poter ricreare gli utenti di prova. Lo script e la pulizia MUST NOT stampare dati
personali.

#### Scenario: Pulizia riuscita
- **WHEN** la pulizia gira sul database della demo
- **THEN** segnalazioni, beneficiari, utenti, aziende e le loro voci dell'audit non ci sono più. Configurazione e
  cataloghi hanno le stesse righe di prima, e le regole di sola aggiunta rifiutano di nuovo le cancellazioni

#### Scenario: Pulizia interrotta
- **WHEN** un passo o un controllo della pulizia fallisce
- **THEN** la transazione si annulla, il database resta quello della demo e lo script dice come riavviarla

#### Scenario: Voci dimostrative rimaste
- **WHEN** la pulizia finisce
- **THEN** elenca, solo con il codice, progetti e tipologie di mansione mai modificati dopo i dati dimostrativi e gli
  enti con i dati inventati, da controllare dai Cataloghi

#### Scenario: Backend senza dati dimostrativi
- **WHEN** dopo la pulizia il backend parte senza il profilo `seed`
- **THEN** Flyway valida le migrazioni e l'applicazione parte

#### Scenario: Keycloak ripartito vuoto
- **WHEN** il database di Keycloak della produzione riparte vuoto e Keycloak importa il realm
- **THEN** il realm non ha utenti, tranne l'account tecnico del backend

#### Scenario: Tornare alla demo
- **WHEN** il passaggio non riesce dopo la pulizia
- **THEN** il PO ripristina il backup fatto all'inizio dello script, seguendo il README, e la demo torna com'era

### Requirement: Ambiente di produzione
Dopo il passaggio il server SHALL partire senza dati dimostrativi: il profilo predefinito del backend MUST NOT
comprendere `seed`, che resta solo per lo sviluppo in locale. Il realm scaricato dal deploy MUST NOT contenere utenti di
prova: il primo ADMIN lo crea il backend con i dati del `.env`. Le password di sviluppo MUST NOT funzionare online.

#### Scenario: Profilo predefinito
- **WHEN** si fa il deploy senza `NEXUS_BACKEND_PROFILES` nel `.env`
- **THEN** il backend parte con i profili `keycloak` e `postgresql`, senza dati dimostrativi

#### Scenario: Password di sviluppo
- **WHEN** si tenta l'accesso a `https://auth.portalenexus.it` come `tutor1` con la password di sviluppo
- **THEN** l'accesso è rifiutato

## MODIFIED Requirements

### Requirement: Segreti fuori dal codice
Password del database e credenziali di amministrazione SHALL essere generate sul server e conservate solo lì, leggibili
solo dall'utente di deploy; MUST NOT comparire nel repository, nelle immagini o nei log della pipeline. La preparazione
di un server nuovo MUST NOT creare utenti di prova né le loro password.

#### Scenario: Immagine pubblicata
- **WHEN** si ispeziona un'immagine pubblicata su GitHub Container Registry
- **THEN** non contiene password né chiavi

#### Scenario: Server nuovo
- **WHEN** `bootstrap.sh` prepara un server nuovo
- **THEN** il `.env` non ha password di utenti di prova e ha le righe vuote dei dati del primo ADMIN, da compilare
  prima del primo deploy

### Requirement: Superficie esposta minima
Dal server SHALL essere raggiungibili da Internet solo SSH e le porte web 80/443. Database, backend e identity provider
MUST NOT essere raggiungibili direttamente. Le API SHALL essere servite sotto `/api` dello stesso dominio dell'app e gli
endpoint tecnici del backend (actuator, documentazione OpenAPI) MUST NOT essere esposti. La console di amministrazione
dell'identity provider e l'accesso dei suoi amministratori (realm `master`) MUST NOT essere esposti su Internet: si
usano solo con un tunnel SSH.

#### Scenario: Database non raggiungibile
- **WHEN** da Internet si tenta una connessione alla porta di PostgreSQL del server
- **THEN** la connessione non riesce

#### Scenario: Console di amministrazione
- **WHEN** da Internet si apre `https://auth.portalenexus.it/admin/`
- **THEN** l'accesso è negato

#### Scenario: Accesso degli amministratori dell'identity provider
- **WHEN** da Internet si chiede un token o la pagina di accesso del realm `master` (`https://auth.portalenexus.it/realms/master/...`)
- **THEN** l'accesso è negato, mentre l'accesso degli utenti di NEXUS (realm `nexus`) resta raggiungibile

#### Scenario: Console dal tunnel SSH
- **WHEN** il PO apre la console con il tunnel SSH (`http://localhost:8081/admin`) e accede come amministratore
- **THEN** l'accesso riesce, perché il tunnel raggiunge l'identity provider senza passare dal reverse proxy

#### Scenario: Endpoint tecnici
- **WHEN** da Internet si apre `https://portalenexus.it/actuator/health` o `https://portalenexus.it/v3/api-docs`
- **THEN** la risorsa non è disponibile

### Requirement: Controllo della disponibilità
Un controllo esterno SHALL verificare ogni 10 minuti, dalle 8 alle 20 ora italiana, che la PWA, Keycloak e il backend
del sito rispondano e che i certificati non scadano entro 14 giorni. Un controllo fallito dopo 3 tentativi SHALL
avvisare il PO per email. Il controllo MUST NOT usare segreti.

#### Scenario: Sito online
- **WHEN** il controllo gira alle 10:00 e sito, Keycloak e API rispondono come previsto
- **THEN** il controllo riesce e nessuno viene avvisato

#### Scenario: Backend giù
- **WHEN** l'API risponde `502` per tre tentativi di fila
- **THEN** il controllo fallisce e il PO riceve un'email da GitHub

#### Scenario: Fuori orario
- **WHEN** il controllo pianificato parte alle 21:30 ora italiana
- **THEN** non controlla nulla e non avvisa

## REMOVED Requirements

### Requirement: Ambiente demo
**Reason**: dal passaggio in produzione (US-1306) il server ha i dati veri: la demo non c'è più.
**Migration**: le regole del server sono in «Ambiente di produzione»; i dati dimostrativi restano per lo sviluppo in
locale, con il profilo `seed`.
