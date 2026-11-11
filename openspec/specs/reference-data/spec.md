# reference-data Specification

## Purpose
Fornisce ai moduli dell'applicazione gli elenchi di riferimento condivisi (zone, tipologie di mansione), limitati alle
voci attive.
## Requirements
### Requirement: Elenchi di riferimento
Il sistema SHALL esporre agli utenti autenticati gli elenchi delle zone e delle tipologie di mansione **attive**, con
identificativo, codice e nome, ordinati per nome.

#### Scenario: Voce disattivata
- **WHEN** una zona è disattivata
- **THEN** non compare nell'elenco delle zone

### Requirement: Zone iniziali
Il sistema SHALL fornire in ogni ambiente, come zone iniziali attive, i quartieri di Firenze con il nome preceduto da
"Firenze " (Firenze Novoli, Firenze Isolotto, Firenze Legnaia, Firenze Rifredi, Firenze Campo di Marte, Firenze Santa
Croce, Firenze Oltrarno, Firenze Galluzzo, Firenze Le Piagge, Firenze Rovezzano, Firenze Le Cure, Firenze Trespiano,
Firenze Sorgane, Firenze Casellina, Firenze Soffiano, Firenze Serpiolle, Firenze Castello, Firenze San Niccolò, Firenze
Varlungo, Firenze Santo Spirito, Firenze Due Strade, Firenze Porta al Prato, Firenze San Giovanni, Firenze Statuto,
Firenze Europa, Firenze Gavinana, Firenze San Lorenzo, Firenze San Frediano, Firenze San Miniato, Firenze San Marco) e i
comuni dell'hinterland senza prefisso (Scandicci, Sesto Fiorentino, Campi Bisenzio, Borgo San Lorenzo, Scarperia e San
Piero, Vicchio, Dicomano, Bagno a Ripoli, Impruneta, Fiesole).

#### Scenario: Ambiente nuovo
- **WHEN** l'applicazione parte su un database vuoto, senza dati demo
- **THEN** l'elenco delle zone contiene le 40 zone iniziali, con i quartieri preceduti da "Firenze "

#### Scenario: Demo esistente
- **WHEN** l'applicazione parte su un database demo con le zone segnaposto
- **THEN** le zone segnaposto sono disattivate e nessun beneficiario o mansione demo vi resta collegato

#### Scenario: Quartiere e comune omonimi
- **WHEN** l'utente apre l'elenco delle zone
- **THEN** distingue "Firenze San Lorenzo" (quartiere) da "Borgo San Lorenzo" (comune)

### Requirement: Gestione dei cataloghi
L'ADMIN SHALL poter aggiungere, rinominare, disattivare e riattivare zone e tipologie di mansione da una pagina
dedicata, senza rilasci. Le voci MUST NOT essere cancellate: una voce disattivata non è più selezionabile per le scelte
nuove ma resta sui dati che la usano. Il codice si ricava dal nome alla creazione e poi non cambia. Tra le voci attive
il nome MUST essere unico, senza distinguere maiuscole e minuscole. Ogni modifica SHALL essere registrata nell'audit.

#### Scenario: Nuova zona
- **WHEN** l'ADMIN aggiunge la zona «Sesto Fiorentino»
- **THEN** la zona è attiva con il codice `SESTO_FIORENTINO`, compare nei menu degli altri moduli e la creazione è
  nell'audit

#### Scenario: Nome già presente
- **WHEN** l'ADMIN aggiunge «fiesole» e la zona attiva «Fiesole» esiste già
- **THEN** il campo segnala che «Fiesole» è già nel catalogo

#### Scenario: Disattivazione
- **WHEN** l'ADMIN disattiva una zona usata da un beneficiario
- **THEN** la zona non compare più nei menu per le scelte nuove, ma il beneficiario la mantiene

#### Scenario: Solo l'ADMIN
- **WHEN** un operatore del Call Center chiede la gestione dei cataloghi
- **THEN** la risposta è `403`

### Requirement: Tipi di tirocinio
L'ADMIN SHALL gestire il catalogo dei tipi di tirocinio con le stesse regole di zone e tipologie: aggiunta, nuovo
nome, disattivazione e riattivazione, audit, nessuna cancellazione. Le voci iniziali SHALL essere Curriculare,
Extracurriculare, Di inclusione e Di inserimento socio-terapeutico. I tipi attivi SHALL essere disponibili per la scheda
del tirocinio.

#### Scenario: Voci iniziali
- **WHEN** l'ADMIN apre la pagina "Cataloghi"
- **THEN** la sezione "Tipi di tirocinio" mostra le quattro voci iniziali, attive

#### Scenario: Tipo disattivato
- **WHEN** l'ADMIN disattiva un tipo
- **THEN** il tipo non è più tra quelli che si possono scegliere nella scheda del tirocinio

### Requirement: Cataloghi del report di restituzione
L'ADMIN SHALL gestire le formule di assunzione e i motivi della mancata assunzione come gli altri cataloghi:
aggiunta, nuovo nome, disattivazione e riattivazione, con l'audit. Il sistema SHALL partire con le voci provvisorie
indicate dal PO (Q6). Il report di restituzione SHALL proporre solo le voci attive.

#### Scenario: Voci iniziali
- **WHEN** l'ADMIN apre la pagina "Cataloghi"
- **THEN** trova le formule (tempo determinato 3, 6 e 12 mesi, tempo indeterminato, contratto a chiamata, prestazione
  occasionale) e i tre motivi della mancata assunzione

#### Scenario: Motivo disattivato
- **WHEN** l'ADMIN disattiva un motivo della mancata assunzione
- **THEN** il motivo non è più tra quelli proposti per il report, ma resta sui report che lo usano

### Requirement: Progetti nei cataloghi
L'ADMIN SHALL gestire i progetti finanziati nella pagina «Cataloghi», con le stesse regole degli altri cataloghi:
aggiunta, nuovo nome, disattivazione e riattivazione, codice ricavato dal nome, nome unico tra i progetti attivi,
audit di ogni modifica. Un progetto disattivato MUST NOT essere selezionabile per una nuova segnalazione né nei
filtri; le segnalazioni che già lo usano MUST restare consultabili con il loro progetto.

#### Scenario: Nuovo progetto
- **WHEN** l'ADMIN aggiunge il progetto «FSE+ Inclusione 2027»
- **THEN** il progetto è attivo con il codice `FSE_INCLUSIONE_2027`, si può assegnare ai Tutor e la creazione è
  nell'audit

#### Scenario: Progetto disattivato
- **WHEN** l'ADMIN disattiva un progetto che ha già delle segnalazioni
- **THEN** il Tutor non lo trova più tra i progetti della nuova segnalazione e una segnalazione con quel progetto è
  rifiutata, ma le segnalazioni esistenti restano consultabili con il loro progetto

### Requirement: Comuni e province
Il sistema SHALL conoscere i comuni e le province italiane con il loro periodo di validità. L'ADMIN SHALL caricarli dal
file dell'ISTAT «Elenco dei comuni italiani», indicando la data da cui il file vale, e SHALL vedere l'anteprima delle
differenze (comuni e province nuovi, soppressi, cambiati di nome, codice o provincia) prima di confermare. Alla conferma
ogni versione soppressa o cambiata MUST chiudersi alla data indicata e la nuova versione MUST aprirsi da quella data;
nessuna versione MUST essere cancellata. Il comune MUST essere identificato dal codice catastale. Un file non
riconosciuto o incompleto, o una data non successiva all'ultimo caricamento, MUST essere rifiutato con il motivo. Ogni
caricamento SHALL essere registrato nell'audit. Gli utenti autenticati SHALL poter cercare i comuni validi per nome,
dalla seconda lettera, con la sigla della provincia.

#### Scenario: Primo caricamento
- **WHEN** l'ADMIN controlla il file ISTAT su un elenco vuoto
- **THEN** l'anteprima indica tutti i comuni e le province come nuovi e, alla conferma, la ricerca «sesto f» trova
  «Sesto Fiorentino (FI)»

#### Scenario: Comune cambiato
- **WHEN** l'ADMIN carica un file in cui un comune ha un nuovo nome o una nuova provincia
- **THEN** l'anteprima lo indica tra i cambiati; dopo la conferma la versione precedente vale fino alla data del
  caricamento e la ricerca trova solo il nuovo nome

#### Scenario: Comune soppresso
- **WHEN** l'ADMIN carica un file senza un comune presente nell'elenco
- **THEN** il comune è tra i soppressi e, dopo la conferma, non compare più nella ricerca ma la sua versione resta

#### Scenario: File sbagliato
- **WHEN** l'ADMIN carica un file che non è l'elenco dell'ISTAT, oppure un elenco con pochi comuni
- **THEN** il caricamento è rifiutato con il motivo e l'elenco non cambia

#### Scenario: Data non valida
- **WHEN** l'ADMIN indica una data non successiva a quella dell'ultimo caricamento
- **THEN** il caricamento è rifiutato

#### Scenario: Solo l'ADMIN carica
- **WHEN** un Tutor chiede di caricare il file
- **THEN** la risposta è `403`; la ricerca invece gli è permessa

### Requirement: Sedi di competenza
L'ADMIN SHALL gestire il catalogo delle sedi di competenza con le regole degli altri cataloghi (aggiunta, nuovo nome,
disattivazione e riattivazione, audit, nessuna cancellazione) e SHALL indicare per ogni sede uno o più progetti. Una
sede MUST NOT essere legata a un comune. Le voci iniziali SHALL essere Firenze, Borgo San Lorenzo e Sesto Fiorentino,
del progetto GOL.

#### Scenario: Sede per più progetti
- **WHEN** l'ADMIN assegna la sede «Firenze» ai progetti GOL e POLIS
- **THEN** la sede vale per entrambi i progetti e la modifica è nell'audit

#### Scenario: Sede disattivata
- **WHEN** l'ADMIN disattiva una sede
- **THEN** non si può più scegliere, ma resta dove è già usata

### Requirement: Tipi di tirocinio per progetto
L'ADMIN SHALL indicare per ogni progetto i tipi di tirocinio ammessi; un progetto senza tipi indicati SHALL ammettere
tutti i tipi attivi. GOL SHALL partire con Extracurriculare e Di inclusione. Ogni modifica SHALL essere nell'audit.

#### Scenario: GOL
- **WHEN** l'ADMIN apre i tipi ammessi di GOL
- **THEN** sono Extracurriculare e Di inclusione

#### Scenario: Nessuna limitazione
- **WHEN** l'ADMIN toglie tutti i tipi a un progetto
- **THEN** il progetto ammette tutti i tipi attivi

### Requirement: Enti di presa in carico
L'ADMIN SHALL gestire il catalogo degli enti che hanno in carico i beneficiari con le regole degli altri cataloghi e,
per ogni ente, SHALL poter indicare tipologia, codice fiscale o partita IVA, sede legale, legale rappresentante (nome,
qualifica, luogo e data di nascita) e referente per la convenzione (nome, telefono, mail); i dati sono facoltativi. Le
voci iniziali SHALL essere Comune di Firenze, Società della Salute di Firenze, Società della Salute Mugello e Società
della Salute Fiorentina Nord Ovest, senza dati di persone. Ogni modifica SHALL essere nell'audit.

#### Scenario: Dati dell'ente
- **WHEN** l'ADMIN indica il referente per la convenzione di un ente
- **THEN** i dati sono salvati e la modifica è nell'audit

#### Scenario: Dati non validi
- **WHEN** l'ADMIN indica una mail del referente non valida o una data di nascita futura
- **THEN** il campo segnala l'errore e nulla è salvato

#### Scenario: Solo l'ADMIN
- **WHEN** un Tutor chiede di modificare un ente o i tipi di un progetto
- **THEN** la risposta è `403`

### Requirement: Comune scelto dall'elenco nei moduli
Dove un modulo chiede il comune di un indirizzo, l'utente SHALL sceglierlo dall'elenco dei comuni validi oggi, con una
casella che propone i comuni dalla seconda lettera con la sigla della provincia (es. «Sesto Fiorentino (FI)»). La
casella MUST potersi usare solo da tastiera e MUST annunciare allo screen reader i suggerimenti, quello attivo e il
numero dei risultati. Il sistema SHALL salvare la versione del comune scelta: un caricamento successivo dell'elenco che
cambia nome o provincia del comune MUST NOT cambiare i dati già salvati. Un comune già salvato SHALL restare valido
anche se la sua versione è stata chiusa; un comune nuovo MUST essere tra quelli validi oggi, altrimenti il salvataggio
è rifiutato con un errore sul campo.

#### Scenario: Scelta da tastiera
- **WHEN** l'utente scrive «sesto f» nella casella del comune, scende con la freccia e preme Invio
- **THEN** il campo mostra «Sesto Fiorentino (FI)» e il comune è quello che verrà salvato

#### Scenario: Testo senza scelta
- **WHEN** l'utente scrive un nome nella casella ma non sceglie un comune dall'elenco e salva
- **THEN** il campo segnala che il comune va scelto dall'elenco e nulla è salvato

#### Scenario: Comune cambiato dopo il salvataggio
- **WHEN** un comune già salvato in un indirizzo cambia provincia con un nuovo caricamento dell'elenco
- **THEN** l'indirizzo salvato mantiene la provincia di prima e, risalvando il modulo senza toccarlo, nulla cambia

#### Scenario: Comune non più valido scelto di nuovo
- **WHEN** un modulo invia, come nuova scelta, una versione di comune già chiusa
- **THEN** il salvataggio è rifiutato con un errore sul campo del comune

