## ADDED Requirements

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
