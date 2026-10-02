## ADDED Requirements

### Requirement: Mansioni dell'azienda
Il sistema SHALL permettere a Call Center e ADMIN di aggiungere mansioni a un'azienda attiva (titolo, tipologia, zona,
descrizione), modificarle, ritirarle e rimetterle a disposizione, con blocco ottimistico e audit di ogni azione. Lo
stato LIBERA/BLOCCATA SHALL NOT essere modificabile a mano.

#### Scenario: Nuova mansione
- **WHEN** un operatore aggiunge una mansione a un'azienda attiva
- **THEN** la mansione è LIBERA e a disposizione, e l'audit registra `CREATE`

#### Scenario: Stato in sola lettura
- **WHEN** una modifica tenta di impostare lo stato BLOCCATA
- **THEN** lo stato resta invariato

#### Scenario: Mansione bloccata
- **WHEN** un operatore tenta di ritirare una mansione bloccata da una segnalazione
- **THEN** la risposta è `409` con il numero della segnalazione, e la mansione resta a disposizione

#### Scenario: Azienda disattivata
- **WHEN** si aggiunge una mansione a un'azienda disattivata
- **THEN** la risposta è `409`
