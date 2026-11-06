## Why

Storia **US-105** (Sprint 9). Quando una persona esce o cambia mansione, oggi l'ADMIN deve passare dalla console di
Keycloak e dal database. Il PO ha deciso il 06/11 che il lavoro aperto non deve restare senza nessuno: le segnalazioni
di un Tutor si spostano prima (US-111); i ticket in carico a un operatore tornano in coda.

## What Changes

- **Pagina «Accesso e ruolo»** (`/utenti/:id/accesso`), dall'elenco degli utenti per ognuno:
  - **disattiva / riattiva**, in NEXUS e in Keycloak; chi è disattivato non accede più e le sue sessioni si chiudono;
  - **cambio di ruolo**, in NEXUS e in Keycloak; vale dal prossimo accesso, le sessioni si chiudono.
- **Regole** (decisioni del PO, 06/11):
  - un **Tutor con segnalazioni aperte** non si disattiva né cambia ruolo: la pagina rimanda allo spostamento;
  - un **operatore con ticket in carico** si disattiva dopo l'avviso; i ticket tornano liberi nella coda «Da prendere
    in carico» e un collega li riprende da dove erano, anche se più avanti nel percorso;
  - chi diventa Tutor lascia in coda i ticket in carico; da Call Center ad ADMIN restano suoi;
  - l'ADMIN non disattiva se stesso né si cambia il ruolo.
- **Ruolo nel token:** a ogni richiesta NEXUS riallinea il ruolo a quello del token; ora un token emesso prima
  dell'ultima modifica dell'utente non lo riporta indietro.
- **Audit:**
  - per l'utente: `DEACTIVATE`, `REACTIVATE`, `ROLE_CHANGE`;
  - per ogni ticket liberato: `RELEASE`, in cronologia «Tornata in coda».
- Se Keycloak non risponde, in NEXUS non cambia nulla (503).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `user-management`: disattivazione, riattivazione e cambio di ruolo.
- `cc-queue`: la coda comprende anche i ticket tornati liberi all'uscita dell'operatore.

## Impact

- Backend:
  - API `GET /users/{id}/impact`, `PUT /users/{id}/active`, `PUT /users/{id}/role`;
  - `IdentityProviderUsers` con stato, ruolo e chiusura delle sessioni;
  - `AuthenticatedUser.issuedAt`.
  - Nessuna migrazione.
- Frontend: pagina `/utenti/:id/accesso`, azione «Accesso e ruolo» nell'elenco, voce di cronologia.
