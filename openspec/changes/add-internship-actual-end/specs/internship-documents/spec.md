## ADDED Requirements

### Requirement: Ruolo nella riga dei tutor aziendali
Nella Convenzione e nel Progetto Formativo ogni tutor aziendale SHALL comparire come nome, ruolo e recapito tra
parentesi, nella forma «Mario Rossi, Responsabile di produzione (055 123456)». Un ruolo o un recapito mancante MUST
essere omesso senza lasciare separatori vuoti.

#### Scenario: Tutor completo
- **WHEN** il tutor aziendale è «Mario Rossi», ruolo «Responsabile di produzione», recapito «055 123456»
- **THEN** il documento riporta «Mario Rossi, Responsabile di produzione (055 123456)»

#### Scenario: Tutor senza ruolo
- **WHEN** il tutor aziendale ha nome e recapito ma non il ruolo
- **THEN** il documento riporta «Mario Rossi (055 123456)»
