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
