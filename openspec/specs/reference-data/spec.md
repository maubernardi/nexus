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

