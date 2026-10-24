# company-registry Specification

## Purpose
Anagrafica delle aziende ospitanti, gestita da Call Center e ADMIN, base per l'abbinamento delle mansioni.
## Requirements
### Requirement: Gestione delle aziende
Il sistema SHALL permettere a Call Center e ADMIN di cercare, registrare, modificare, disattivare e riattivare le
aziende, senza mai cancellarle. Il Tutor SHALL NOT accedere all'anagrafica.

#### Scenario: Registrazione
- **WHEN** un operatore registra un'azienda con ragione sociale e P.IVA valide
- **THEN** l'azienda è attiva e l'audit registra `CREATE` con autore e dati

#### Scenario: Disattivazione
- **WHEN** un operatore disattiva un'azienda
- **THEN** non compare più nella ricerca predefinita, resta consultabile tra le disattivate e l'audit registra
  `DEACTIVATE`

#### Scenario: Accesso del Tutor
- **WHEN** un Tutor richiede l'anagrafica delle aziende
- **THEN** la risposta è `403`

### Requirement: Partita IVA valida e univoca
Il sistema SHALL accettare solo P.IVA di 11 cifre e SHALL rifiutare una P.IVA già usata da un'altra azienda con un
errore sul campo.

#### Scenario: P.IVA duplicata
- **WHEN** si registra o modifica un'azienda con la P.IVA di un'altra
- **THEN** la risposta è `400` con errore sul campo `vatCode`

### Requirement: Modifiche concorrenti e tracciate
Il sistema SHALL rifiutare con `409` una modifica basata su una versione superata e SHALL registrare nell'audit solo i
campi effettivamente cambiati, con i valori prima e dopo.

#### Scenario: Due operatori sulla stessa azienda
- **WHEN** due operatori modificano la stessa azienda partendo dalla stessa versione
- **THEN** il primo salva, il secondo riceve `409` e un messaggio che lo invita a ricaricare

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

### Requirement: Disattivazione con proposte in corso
Il sistema SHALL rifiutare con `409` la disattivazione di un'azienda che ha mansioni BLOCCATE da segnalazioni, indicando
per ciascuna il titolo della mansione e il numero della segnalazione; l'azienda resta attiva.

#### Scenario: Mansione bloccata
- **WHEN** un operatore disattiva un'azienda la cui mansione «Magazziniere» è bloccata dalla segnalazione n. 30
- **THEN** la risposta è `409` con il messaggio "Non si può disattivare l'azienda: la mansione «Magazziniere» è bloccata
  dalla segnalazione n. 30" e la scheda azienda lo mostra

### Requirement: Diario delle note dell'azienda
Il sistema SHALL consentire a Call Center e ADMIN di aggiungere note di testo (obbligatorio, al massimo 2000 caratteri)
alla scheda di un'azienda e di consultarle dalla più recente, con data, ora e autore. Le note MUST NOT essere modificate
né cancellate, e il database SHALL rifiutarne modifica, cancellazione e svuotamento. L'aggiunta SHALL essere registrata
nell'audit senza il testo della nota.

#### Scenario: Aggiunta di una nota
- **WHEN** un operatore aggiunge la nota "Chiusura per ferie ad agosto"
- **THEN** la nota compare in cima al diario con data, ora e nome dell'operatore, e l'audit registra `NOTE_ADD` senza testo

#### Scenario: Nota vuota
- **WHEN** l'operatore invia una nota vuota o di soli spazi
- **THEN** il sistema la rifiuta indicando il campo

#### Scenario: Note immodificabili
- **WHEN** si tenta di modificare o cancellare una nota direttamente nel database
- **THEN** l'operazione è rifiutata

#### Scenario: Tutor
- **WHEN** un Tutor chiede le note di un'azienda
- **THEN** la risposta è `403`

