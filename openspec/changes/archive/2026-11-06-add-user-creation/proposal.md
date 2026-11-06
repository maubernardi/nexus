## Why

Storia **US-103** (Sprint 9). Oggi un nuovo Tutor o operatore si crea a mano in due posti: l'account nella console di
Keycloak e la riga in `app_user` con lo stesso identificativo. Obiettivo dello Sprint 9: l'ADMIN gestisce gli utenti
da NEXUS, senza la console.

## What Changes

- **Pagina «Utenti»** per l'ADMIN (`/utenti`, nuova voce di menu): elenco con ruolo e stato.
- **Nuovo utente** (`/utenti/nuovo`): nome, cognome, username, email, telefono facoltativo, ruolo.
  - Lo username lo scrive l'ADMIN, con la proposta `nome.cognome` (decisione del PO, §8.8 della review dello Sprint 8).
  - L'utente nasce **in Keycloak e in NEXUS con lo stesso identificativo** e il ruolo scelto.
  - Se Keycloak crea l'account ma NEXUS non lo salva, l'account Keycloak si cancella: nessuno stato incoerente.
- **Email per impostare la password** (decisione §8.7): la invia Keycloak con lo stesso SMTP di IONOS, con un
  collegamento valido **3 giorni** (decisione del PO, 06/11) che dopo porta all'app.
  - Se l'email non parte, l'utente resta creato e l'elenco offre **«Invia di nuovo l'invito»**.
- **Audit:** creazione (username e ruolo in chiaro, dati personali senza valori) e ogni invito inviato.
- **Keycloak:**
  - il client tecnico `nexus-backend-admin` (US-110) crea, cancella e invita gli utenti con i permessi che ha già;
  - il ruolo si assegna leggendo i ruoli disponibili dell'utente, senza permessi in più;
  - il realm della demo ha il server di posta, con la password da `NEXUS_SMTP_PASSWORD`;
  - per il server già installato, uno script una tantum `keycloak-email.sh`.
- L'assegnazione ai progetti è US-104; disattivazione e cambio ruolo US-105.

## Capabilities

### New Capabilities
- `user-management`: gestione degli utenti da parte dell'ADMIN, senza la console di Keycloak.

### Modified Capabilities
<!-- nessuna -->

## Impact

- Backend: `/api/v1/users` (elenco, creazione, nuovo invito), `IdentityProviderUsers` esteso. Nessuna migrazione.
- Frontend: pagine `/utenti` e `/utenti/nuovo`, voce di menu «Utenti».
- Deploy: SMTP nel realm demo, `NEXUS_SMTP_PASSWORD` a Keycloak nel compose, `keycloak-email.sh`.
