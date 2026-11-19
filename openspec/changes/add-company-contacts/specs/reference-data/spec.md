## ADDED Requirements

### Requirement: Ruoli dei referenti
L'ADMIN SHALL gestire i ruoli dei referenti delle aziende come gli altri cataloghi: aggiunta, rinomina,
disattivazione e riattivazione, con l'audit. Le voci iniziali MUST essere i 12 ruoli del database attuale, ripuliti
dalle righe vuote, da quelle che non sono ruoli e dai doppioni. L'id di ogni voce iniziale MUST essere
893700000000000500 più il numero del ruolo nel database attuale, perché l'importazione lo ritrovi anche dopo una
rinomina. Il Call Center e l'ADMIN SHALL ricevere i ruoli attivi per il modulo dell'azienda; il Tutor MUST NOT
riceverli.

#### Scenario: Voci iniziali
- **WHEN** l'ADMIN apre il catalogo dei ruoli dei referenti
- **THEN** trova 12 voci attive, tra cui «Titolare», «Responsabile del personale» e «Legale rappresentante»

#### Scenario: Nuovo ruolo
- **WHEN** l'ADMIN aggiunge «Responsabile di cucina»
- **THEN** la voce è attiva, il Call Center la trova nel modulo dell'azienda e la creazione è nell'audit

#### Scenario: Tutor
- **WHEN** un Tutor chiede i ruoli dei referenti
- **THEN** la risposta è `403`
