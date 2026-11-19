## ADDED Requirements

### Requirement: Categorie merceologiche
L'ADMIN SHALL gestire le categorie merceologiche delle aziende come gli altri cataloghi: aggiunta, rinomina,
disattivazione e riattivazione, con l'audit. Le voci iniziali MUST essere le 53 categorie del database attuale, con
due nomi corretti (refusi e spaziature). L'id di ogni voce iniziale MUST essere 893700000000000400 più il numero della
categoria nel database attuale, perché l'importazione la ritrovi anche dopo una rinomina. Il Call Center e l'ADMIN
SHALL ricevere le categorie attive per i loro moduli e filtri; il Tutor MUST NOT riceverle.

#### Scenario: Voci iniziali
- **WHEN** l'ADMIN apre il catalogo delle categorie merceologiche
- **THEN** trova 53 voci attive, tra cui «Albergo», «Ristorante / Trattoria / Bistrot» e «Pulizie e facchinaggio»

#### Scenario: Nuova categoria
- **WHEN** l'ADMIN aggiunge «Agenzia immobiliare»
- **THEN** la voce è attiva, il Call Center la trova nel modulo dell'azienda e la creazione è nell'audit

#### Scenario: Categoria disattivata
- **WHEN** l'ADMIN disattiva «Pensione», che è la categoria di un'azienda
- **THEN** «Pensione» non si sceglie più per le altre aziende, ma l'azienda la mantiene

#### Scenario: Tutor
- **WHEN** un Tutor chiede le categorie merceologiche
- **THEN** la risposta è `403`
