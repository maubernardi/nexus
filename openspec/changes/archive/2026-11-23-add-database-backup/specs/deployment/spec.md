## ADDED Requirements

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
