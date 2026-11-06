## Why

Storia **US-110**. Le email delle notifiche (US-1202) partono verso l'indirizzo dell'anagrafica utenti, ma oggi non c'è
modo di cambiarlo dall'app: serve un comando SQL sul server o la console di Keycloak, e le due copie dell'indirizzo
(NEXUS e Keycloak) possono divergere. Il PO vuole che ogni utente cambi da sé il proprio indirizzo, in modo sicuro:
il nuovo indirizzo vale solo dopo la conferma con un codice monouso (OTP) inviato a quell'indirizzo.

## What Changes

- **Pagina «Il mio profilo»** (`/profilo`) per tutti i ruoli, raggiungibile dal nome dell'utente nell'intestazione
  e nel menu mobile: mostra nome, ruolo e indirizzo email.
- **Cambio dell'indirizzo in due passi** (decisioni del PO, 05/11):
  - si scrive il nuovo indirizzo: NEXUS invia a quell'indirizzo un codice di **6 cifre**, valido **30 minuti**;
  - si scrive il codice: solo allora l'indirizzo cambia, **in NEXUS e in Keycloak** (dove serve anche per accedere);
  - **al vecchio indirizzo** arriva un avviso del cambio («se non sei stato tu, contatta l'amministratore»).
- **Protezioni**:
  - al massimo **5 tentativi** per codice, poi se ne chiede uno nuovo;
  - un nuovo codice si può chiedere dopo **60 secondi**;
  - il codice si salva solo cifrato (hash BCrypt), mai nei log;
  - una sola richiesta in corso per utente: una nuova sostituisce la precedente; si può annullare;
  - un indirizzo già usato da un altro utente è rifiutato.
- **Audit** (EN-2): il cambio confermato si registra sull'utente, senza i valori (dato personale).
- **Keycloak**: il backend aggiorna l'utente con la API di amministrazione, tramite un client tecnico dedicato
  (`nexus-backend-admin`, solo i ruoli `view-users` e `manage-users`). Il segreto non è nel repository.
  - nuove installazioni: il client è nel realm della demo e `bootstrap.sh` genera il segreto;
  - server esistente: uno script una tantum (`keycloak-backend-client.sh`) crea il client e scrive il segreto nel `.env`.
- In locale (utenti mock, senza Keycloak né SMTP) il codice compare nel log del backend, così si prova il flusso.
- Nessuna nuova dipendenza: la API di Keycloak si chiama con `RestClient` di Spring.

## Capabilities

### New Capabilities
- `user-profile`: pagina del profilo e cambio del proprio indirizzo email confermato con OTP.

### Modified Capabilities
<!-- nessuna -->

## Impact

- Backend: migrazione **V27** (`email_change_request`), endpoint `/api/v1/me/email…`, client della API di Keycloak.
- Frontend: pagina `/profilo`, collegamento dal nome dell'utente.
- Deploy: `compose.yaml` (segreto del client per Keycloak e backend), realm della demo, `bootstrap.sh`, nuovo script.
