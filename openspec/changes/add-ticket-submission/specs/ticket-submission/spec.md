## Purpose

Permette al Tutor di segnalare un proprio candidato per un progetto a cui è assegnato, avviando il percorso di tirocinio.

## ADDED Requirements

### Requirement: Invio della segnalazione
Il sistema SHALL permettere al solo Tutor di inviare una segnalazione normale indicando un proprio candidato, un progetto
a cui è assegnato e una tipologia di mansione attiva del catalogo. Il ticket SHALL nascere di tipo `NORMAL`, senza
fast-track, in stato `NUOVA`, con un numero progressivo, e con la creazione registrata nella cronologia.

#### Scenario: Segnalazione riuscita
- **WHEN** il Tutor invia una segnalazione valida
- **THEN** la risposta è `201` con il ticket in stato `NUOVA` e il suo numero, e il ticket compare tra quelli da lavorare
  del Call Center

#### Scenario: Progetto non assegnato
- **WHEN** il Tutor indica un progetto a cui non è assegnato, o disattivato
- **THEN** la risposta è `400` con un errore sul campo `projectId` e nessun ticket viene creato

#### Scenario: Candidato di un altro tutor
- **WHEN** il Tutor indica un candidato inesistente o di cui non è proprietario
- **THEN** la risposta è `400` con un errore sul campo `candidateId`, senza rivelare se il candidato esiste

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
riceve il focus, uso completo da tastiera e a 320 px) e SHALL spiegare cosa fare quando il Tutor non ha candidati o
progetti.

#### Scenario: Nessun candidato
- **WHEN** il Tutor non ha ancora candidati
- **THEN** la pagina lo spiega e offre il collegamento a "Nuovo candidato" al posto del modulo

#### Scenario: Conferma
- **WHEN** la segnalazione è inviata
- **THEN** il focus va alla conferma, che riporta numero e stato in testo
