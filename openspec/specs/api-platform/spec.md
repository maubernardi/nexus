# api-platform Specification

## Purpose
Definisce il contratto trasversale delle API REST di NEXUS: autenticazione e autorizzazione, formato
uniforme degli errori, health check e identità dell'utente corrente, su cui si appoggiano tutte le feature di dominio.
## Requirements
### Requirement: Autenticazione obbligatoria (deny by default)
Il sistema SHALL rifiutare con HTTP 401 ogni richiesta a un endpoint `/api/**` priva di credenziali valide,
ad eccezione degli endpoint pubblici esplicitamente dichiarati (health check e documentazione OpenAPI).

#### Scenario: Richiesta senza token
- **WHEN** un client invoca `GET /api/v1/me` senza credenziali
- **THEN** il sistema risponde HTTP 401 con corpo nel formato errori uniforme

#### Scenario: Health check pubblico
- **WHEN** un client invoca l'endpoint di health senza credenziali
- **THEN** il sistema risponde HTTP 200 con stato `UP` quando database e applicazione sono operativi

### Requirement: Ruoli applicativi
Il sistema SHALL riconoscere esclusivamente i ruoli `TUTOR`, `CALL_CENTER` e `ADMIN`, ricavati dal token
dell'identity provider, e SHALL rispondere HTTP 403 quando un utente autenticato invoca un endpoint
non consentito al suo ruolo.

#### Scenario: Ruolo non autorizzato
- **WHEN** un utente con solo ruolo `TUTOR` invoca un endpoint riservato ad `ADMIN`
- **THEN** il sistema risponde HTTP 403 con corpo nel formato errori uniforme

### Requirement: Identità dell'utente corrente
Il sistema SHALL esporre `GET /api/v1/me` che restituisce identificativo, username, nome, cognome, email
e ruoli applicativi dell'utente autenticato.

#### Scenario: Utente autenticato
- **WHEN** un utente autenticato con ruolo `CALL_CENTER` invoca `GET /api/v1/me`
- **THEN** il sistema risponde HTTP 200 con i suoi dati anagrafici e `roles` contenente `CALL_CENTER`

### Requirement: Formato errori uniforme
Ogni risposta di errore delle API SHALL avere corpo JSON con i campi `timestamp`, `status`, `error`,
`message`, `path` e, per errori di validazione, `fieldErrors` (lista di `field` + `message`).
Le risposte di errore MUST NOT includere stack trace o dettagli interni.

#### Scenario: Risorsa inesistente
- **WHEN** un client autenticato richiede una risorsa che non esiste
- **THEN** il sistema risponde HTTP 404 con corpo nel formato errori uniforme

#### Scenario: Errore interno
- **WHEN** si verifica un errore inatteso durante l'elaborazione
- **THEN** il sistema risponde HTTP 500 con un messaggio generico, senza stack trace

### Requirement: Documentazione API
Il sistema SHALL pubblicare la specifica OpenAPI delle API e un'interfaccia di consultazione interattiva
negli ambienti di sviluppo.

#### Scenario: Specifica OpenAPI disponibile
- **WHEN** uno sviluppatore richiede la specifica OpenAPI in ambiente locale
- **THEN** il sistema restituisce un documento OpenAPI 3 che elenca `GET /api/v1/me`

