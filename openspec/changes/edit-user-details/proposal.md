## Why

Storia **US-116** (Sprint 17), chiesta dal PO il 24/11: in produzione ha creato un utente con lo username sbagliato.
Oggi da NEXUS non si può correggere nessun dato di un utente già creato: si può solo disattivarlo e crearne un altro,
ma con un'altra email, perché quella vecchia resta all'utente disattivato.

Le prove sul collaudo del 24/11 mostrano altri due punti:
- Keycloak non lascia cambiare lo username nemmeno all'account tecnico di NEXUS (`error-user-attribute-read-only`):
  nel realm manca «Edit username»;
- **buco di sicurezza**: dalla pagina «il mio account» di Keycloak un utente creato da NEXUS cambia da solo nome,
  cognome ed email (risposta 204). Il cambio dell'email salta il codice di US-110, e NEXUS e Keycloak restano con dati
  diversi. Riguarda tutti gli utenti veri della produzione.

Il PO ha scelto (24/11):
- la storia subito, nello Sprint 17;
- l'ADMIN corregge username, nome, cognome, email e telefono;
- lo username solo finché l'utente non è mai entrato.

## What Changes

- **Dati dell'utente**: dall'elenco «Utenti» l'ADMIN apre «Dati» di un utente e corregge nome, cognome, username,
  email e telefono. NEXUS li aggiorna anche in Keycloak; se Keycloak non risponde, non cambia nulla.
- **Username solo prima del primo accesso**: si corregge finché l'utente non ha impostato la password. Dopo il campo è
  in sola lettura e la pagina spiega il motivo: l'audit registra l'autore delle azioni con lo username.
- **Email**:
  - unica, come alla creazione;
  - se l'utente non è ancora entrato, NEXUS gli rimanda l'invito al nuovo indirizzo. Il collegamento spedito al
    vecchio indirizzo non vale più (Keycloak lo rifiuta, provato sul collaudo);
  - se l'utente è già entrato, il vecchio indirizzo riceve l'avviso del cambio, come in US-110;
  - una richiesta di cambio di US-110 ancora in attesa si annulla.
- **Audit**: la correzione si registra con i campi cambiati. Lo username è in chiaro, come alla creazione; gli altri
  dati personali sono oscurati.
- **Keycloak**:
  - «Edit username» attivo nel realm, così NEXUS può correggerlo;
  - username, email, nome e cognome si modificano solo con la API di amministrazione. Dalla pagina «il mio account»
    restano in sola lettura: si chiude il buco.
  - In produzione la configurazione la applica il PO con lo script una tantum `infra/deploy/keycloak-user-profile.sh`.
    Il realm di produzione (nuove installazioni), quello di sviluppo e quello del collaudo nascono già così.
  - La CI verifica il realm.

## Non-goals

- Correggere lo username di chi è già entrato: servirebbe riscrivere l'autore nell'audit, che è immodificabile.
- Cancellare un utente: un utente sbagliato si disattiva, come oggi.
- Togliere agli utenti la pagina «il mio account» di Keycloak (ruolo `manage-account`): NEXUS non la usa, ma basta
  renderne i dati in sola lettura.
- Cambiare il ruolo, l'accesso o i progetti: restano nelle loro pagine (US-104, US-105).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `user-management`: nuovo requisito «Correzione dei dati dell'utente».
- `user-profile`: i dati dell'account non si cambiano dalla pagina dell'account di Keycloak, solo da NEXUS.

## Impact

- **Backend**:
  - `UserAdminService` e `UserResource`: lettura e correzione dei dati, `GET` e `PUT /api/v1/users/{id}/details`;
  - `IdentityProviderUsers`: correzione dei dati e «ha già una password», con le versioni Keycloak e locale;
  - audit `DETAILS_CHANGED`; avviso al vecchio indirizzo con l'evento di US-110.
- **Frontend**: pagina «Dati dell'utente» (`/utenti/:id/dati`), pulsante «Dati» nell'elenco, testi in `it.ts`.
  - **Accessibilità** (WCAG 2.2 AA): il modulo segue «Nuovo utente», con il riepilogo degli errori, i campi con le
    etichette e lo username in sola lettura spiegato da un testo collegato al campo; test axe.
  - **PWA**: nessuna nuova cache; le API restano fuori dal service worker.
- **Keycloak**: `infra/deploy/keycloak/nexus-realm.json`, `infra/keycloak/nexus-realm.json`, nuovo script
  `infra/deploy/keycloak-user-profile.sh`, controllo nel job «Tema di Keycloak» della CI.
- **Documenti**: collaudo T-34, README (script una tantum), backlog US-116, sprint 17.
- **Nessuna migrazione.**
