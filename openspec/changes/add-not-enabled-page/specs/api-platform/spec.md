## MODIFIED Requirements

### Requirement: Accesso riservato agli utenti censiti
Il sistema SHALL consentire l'accesso agli endpoint protetti solo agli utenti autenticati che sono censiti e attivi in
NEXUS; negli altri casi MUST rispondere HTTP 403 nel formato errori uniforme con il codice `USER_NOT_ENABLED`, qualunque
sia il punto in cui il controllo fallisce. Gli altri 403 (ruolo non autorizzato) SHALL NOT riportare questo codice. Il
ruolo resta definito dall'identity provider: se il ruolo registrato in NEXUS è diverso da quello del token, il sistema
SHALL aggiornare la copia in NEXUS.

#### Scenario: Utente non censito
- **WHEN** un utente con credenziali valide ma assente in NEXUS invoca `GET /api/v1/me`
- **THEN** il sistema risponde HTTP 403 con corpo nel formato errori uniforme e codice `USER_NOT_ENABLED`

#### Scenario: Utente disattivato
- **WHEN** un utente censito ma disattivato invoca un endpoint protetto
- **THEN** il sistema risponde HTTP 403 con codice `USER_NOT_ENABLED`

#### Scenario: Ruolo non autorizzato
- **WHEN** un utente abilitato invoca un endpoint non consentito al suo ruolo
- **THEN** il sistema risponde HTTP 403 senza codice `USER_NOT_ENABLED`

#### Scenario: Ruolo cambiato nell'identity provider
- **WHEN** un utente censito come `TUTOR` si presenta con un token che riporta il ruolo `CALL_CENTER`
- **THEN** la richiesta è autorizzata come `CALL_CENTER` e il ruolo registrato in NEXUS diventa `CALL_CENTER`
