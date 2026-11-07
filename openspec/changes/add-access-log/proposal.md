## Why

Storia **US-113** (Sprint 10), appunto del PO del 06/11: per ogni utente deve restare il registro degli accessi, con
data, ora e indirizzo IP. Decisioni del PO (06/11 e review dello Sprint 9, §8.4):
- anche i tentativi falliti, distinti;
- lo vede l'ADMIN per ogni utente e ogni utente per sé;
- una pagina dell'ADMIN con tutti i tentativi falliti, anche con uno username inesistente;
- conservazione 12 mesi, poi cancellazione automatica (l'IP è un dato personale).

## What Changes

- **Registro in Keycloak**: gli eventi `LOGIN` e `LOGIN_ERROR` del realm si salvano con data, ora, IP (da
  `X-Forwarded-For`, dietro Caddy) e motivo; Keycloak li cancella dopo 12 mesi (`eventsExpiration`). NEXUS non ne fa
  copia.
- **Lettura da NEXUS** con il client tecnico `nexus-backend-admin`, che riceve il permesso `view-events`.
- **Pagine**:
  - «Accessi di …» (`/utenti/:id/accessi`, ADMIN), dal pulsante **Accessi** nell'elenco degli utenti;
  - «Accessi non riusciti» (`/utenti/accessi-falliti`, ADMIN), con chi, cosa ha scritto nel modulo e da dove;
  - «I miei accessi» nel profilo di ogni utente.
  Data e ora italiane, esito con il motivo (password sbagliata, utente inesistente, utente disattivato, bloccato per
  troppi tentativi), 20 alla volta con «Mostra accessi più vecchi».
- Se Keycloak non risponde: `503` con un messaggio e «Riprova».

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `user-management`: requisito aggiunto per il registro degli accessi.

## Impact

- Backend: `GET /users/{id}/access-log`, `GET /me/access-log`, `GET /access-log/failed` (`?page=`);
  `IdentityProviderUsers.loginEvents/failedLogins`.
- Realm di sviluppo e della demo: eventi, scadenza, `view-events`. Sul server già installato: script una tantum
  `infra/deploy/keycloak-access-log.sh` (nessun segreto), dopo il deploy. Gli accessi precedenti non ci sono.
- Nessuna migrazione.
