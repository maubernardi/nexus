## Purpose

Modelli Word dei documenti del tirocinio (convenzione, progetto formativo, relazione finale) caricati dall'ADMIN con
le condizioni in cui valgono, senza rilasci e fuori dal repository, con il controllo dei segnaposto e l'audit.

## ADDED Requirements

### Requirement: Caricamento dei modelli
L'ADMIN SHALL poter caricare un modello Word (.docx, al massimo 5 MB) dalla pagina «Modelli dei documenti». Per ogni
modello indica:
- il documento: convenzione, progetto formativo o relazione finale;
- il progetto;
- il tipo di tirocinio, tra quelli ammessi dal progetto;
- facoltativamente, l'ente di presa in carico;
- un nome che lo distingue.

Il file MUST essere salvato nel database, così entra nel backup e resta fuori dal repository. Gli altri utenti MUST
NOT vedere né gestire i modelli.

#### Scenario: Modello caricato
- **WHEN** l'ADMIN carica la convenzione «Convenzione GOL extracurriculare» per il progetto GOL e il tipo
  Extracurriculare, senza ente
- **THEN** il modello compare nell'elenco come attivo, con documento, progetto, tipo, «tutti gli enti» e la data di
  caricamento

#### Scenario: Tipo non ammesso dal progetto
- **WHEN** l'ADMIN sceglie un tipo di tirocinio che il progetto non ammette
- **THEN** il caricamento è rifiutato con l'errore sul tipo

#### Scenario: Altri utenti
- **WHEN** un Tutor o un operatore del Call Center chiede l'elenco dei modelli o prova a caricarne uno
- **THEN** la richiesta è rifiutata

### Requirement: Controllo del file e dei segnaposto
Al caricamento e alla sostituzione NEXUS MUST controllare che il file sia un documento Word (.docx) leggibile e che
usi solo i segnaposto che sa compilare. Altrimenti il file è rifiutato e il messaggio elenca i segnaposto sconosciuti.
La pagina SHALL mostrare l'elenco dei segnaposto che si possono usare.

#### Scenario: Segnaposto sconosciuto
- **WHEN** l'ADMIN carica un modello che contiene `{CUP}` e `{DATA_INIZIO}`
- **THEN** il caricamento è rifiutato con il messaggio che `{CUP}` non è un segnaposto noto

#### Scenario: File non Word
- **WHEN** l'ADMIN carica un PDF o un .doc del vecchio formato
- **THEN** il caricamento è rifiutato: il file deve essere un documento Word .docx

### Requirement: Sostituzione e disattivazione dei modelli
L'ADMIN SHALL poter scaricare il file di un modello e sostituirlo con uno nuovo, con gli stessi controlli. SHALL
anche poter disattivare un modello, che non si sceglie più, e riattivarlo. Le condizioni di un modello MUST NOT
cambiare dopo il caricamento. Sostituire o disattivare un modello MUST NOT cambiare i documenti già scaricati.

#### Scenario: Sostituzione del file
- **WHEN** l'ADMIN sostituisce il file di un modello con una versione corretta
- **THEN** i documenti scaricati da quel momento usano il file nuovo

#### Scenario: Modello disattivato
- **WHEN** l'ADMIN disattiva l'unico modello che vale per una segnalazione
- **THEN** il Tutor scarica il documento con il modello generico

### Requirement: Audit dei modelli
Ogni caricamento, sostituzione, disattivazione e riattivazione SHALL essere registrato nell'audit con autore e istante.
Il caricamento si registra con documento, progetto, tipo, ente, nome e dimensione del file; la sostituzione con la
dimensione prima e dopo.

#### Scenario: Audit del caricamento
- **WHEN** l'ADMIN carica un modello
- **THEN** l'audit registra il caricamento con le condizioni e la dimensione del file
