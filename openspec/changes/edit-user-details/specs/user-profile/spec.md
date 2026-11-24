## ADDED Requirements

### Requirement: Dati dell'account modificabili solo da NEXUS
Username, nome, cognome ed email di un utente MUST cambiare solo tramite NEXUS: il cambio dell'email con il codice
(US-110), le correzioni con la pagina «Dati» dell'ADMIN. Dalla pagina dell'account di Keycloak l'utente MUST NOT
poterli modificare; MAY solo vederli. La regola MUST valere anche per gli utenti già esistenti quando viene applicata.

#### Scenario: Pagina dell'account di Keycloak
- **WHEN** un utente prova a cambiare il proprio nome, username o email dalla pagina dell'account di Keycloak
- **THEN** Keycloak rifiuta il cambio e i dati restano uguali in NEXUS e in Keycloak

#### Scenario: Correzione dell'ADMIN
- **WHEN** l'ADMIN corregge da NEXUS i dati di un utente
- **THEN** Keycloak accetta la correzione con l'account tecnico di NEXUS
