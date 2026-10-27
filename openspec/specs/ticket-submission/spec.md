# ticket-submission Specification

## Purpose
Permette al Tutor di segnalare un proprio beneficiario per un progetto a cui è assegnato, avviando il percorso di tirocinio.
## Requirements
### Requirement: Invio della segnalazione
Il sistema SHALL permettere al solo Tutor di inviare una segnalazione normale indicando un proprio beneficiario, un progetto
a cui è assegnato e una tipologia di mansione attiva del catalogo. Il ticket SHALL nascere di tipo `NORMAL`, senza
fast-track, in stato `NUOVA`, con un numero progressivo, e con la creazione registrata nella cronologia.

#### Scenario: Segnalazione riuscita
- **WHEN** il Tutor invia una segnalazione valida
- **THEN** la risposta è `201` con il ticket in stato `NUOVA` e il suo numero, e il ticket compare tra quelli da lavorare
  del Call Center

#### Scenario: Progetto non assegnato
- **WHEN** il Tutor indica un progetto a cui non è assegnato, o disattivato
- **THEN** la risposta è `400` con un errore sul campo `projectId` e nessun ticket viene creato

#### Scenario: Beneficiario di un altro tutor
- **WHEN** il Tutor indica un beneficiario inesistente o di cui non è proprietario
- **THEN** la risposta è `400` con un errore sul campo `beneficiaryId`, senza rivelare se il beneficiario esiste

#### Scenario: Ruolo non Tutor
- **WHEN** un operatore Call Center o un ADMIN invia una segnalazione
- **THEN** la risposta è `403`

### Requirement: Progetti dell'utente
Il sistema SHALL esporre all'utente autenticato l'elenco dei progetti attivi a cui è assegnato, ordinati per nome.

#### Scenario: Scelta del progetto
- **WHEN** il Tutor apre il modulo di segnalazione
- **THEN** può scegliere solo tra i progetti attivi a cui è assegnato

### Requirement: Modulo di segnalazione accessibile
Il modulo SHALL rispettare le regole dei moduli accessibili (etichette visibili, errori collegati ai campi, riepilogo che
riceve il focus, uso completo da tastiera e a 320 px) e SHALL spiegare cosa fare quando il Tutor non ha beneficiari o
progetti.

#### Scenario: Nessun beneficiario
- **WHEN** il Tutor non ha ancora beneficiari
- **THEN** la pagina lo spiega e offre il collegamento a "Nuovo beneficiario" al posto del modulo

#### Scenario: Conferma
- **WHEN** la segnalazione è inviata
- **THEN** il focus va alla conferma, che riporta numero e stato in testo

### Requirement: Una sola segnalazione aperta per beneficiario
Il sistema SHALL impedire di inviare una segnalazione per un beneficiario che ne ha già una aperta, cioè in qualunque
stato diverso da `FORM_RESTITUZIONE`. Il modulo SHALL mostrare i beneficiari con una segnalazione aperta come non
selezionabili, indicandone il numero.

#### Scenario: Segnalazione già aperta
- **WHEN** il Tutor invia una segnalazione per un beneficiario che ha la segnalazione n. N aperta
- **THEN** la risposta è `400` con un errore sul campo `beneficiaryId` che cita la segnalazione n. N, e nessun ticket
  viene creato

#### Scenario: Percorso concluso
- **WHEN** l'unica segnalazione del beneficiario è in `FORM_RESTITUZIONE`
- **THEN** il Tutor può inviarne una nuova

#### Scenario: Invii concorrenti
- **WHEN** due richieste per lo stesso beneficiario superano insieme il controllo
- **THEN** il database ne accetta una sola e l'altra riceve `409`

#### Scenario: Modulo
- **WHEN** il Tutor apre il modulo e un suo beneficiario ha la segnalazione n. N aperta
- **THEN** quel beneficiario compare come non selezionabile con il testo "segnalazione n. N aperta"; se tutti i suoi
  beneficiari sono in questa situazione, la pagina lo spiega al posto del modulo

### Requirement: Nuova tipologia di mansione dal Tutor
Il Tutor SHALL poter descrivere una tipologia di mansione che non è nel catalogo al posto di sceglierne una; la
segnalazione SHALL nascere in `IN_ATTESA_APPROVAZIONE` e non entrare in coda. Una tipologia già nel catalogo (stesso
nome, senza distinguere maiuscole) MUST essere scelta dall'elenco. Un operatore del Call Center o l'ADMIN SHALL poter:
approvarla, anche correggendone il nome, e allora la tipologia entra nel catalogo; oppure respingerla scegliendo una
tipologia del catalogo con un motivo obbligatorio. In entrambi i casi la segnalazione passa in `NUOVA` (coda) e la
decisione è registrata nell'audit e in cronologia; il Tutor della segnalazione ne riceve una notifica in app.

#### Scenario: Invio con tipologia nuova
- **WHEN** il Tutor descrive la tipologia "Tecnico del suono" e invia
- **THEN** la segnalazione è "In attesa di approvazione" e il Call Center la trova in "Da approvare", non in coda

#### Scenario: Approvazione con nome corretto
- **WHEN** l'operatore approva la tipologia con il nome "Tecnico audio"
- **THEN** "Tecnico audio" entra nel catalogo, la segnalazione va in coda con quella tipologia e la cronologia mostra
  "Nuova tipologia approvata"

#### Scenario: Respingimento
- **WHEN** l'operatore sceglie "Magazziniere" dal catalogo con il motivo "Rientra nel catalogo"
- **THEN** la segnalazione va in coda come "Magazziniere", la cronologia mostra proposta → scelta e il motivo, e il Tutor
  riceve la notifica "tipologia cambiata"

#### Scenario: Tipologia già nel catalogo
- **WHEN** il Tutor descrive una tipologia con il nome di una già presente
- **THEN** il sistema chiede di sceglierla dall'elenco

#### Scenario: Altri ruoli
- **WHEN** un Tutor prova ad approvare o respingere una tipologia
- **THEN** la risposta è `403`

