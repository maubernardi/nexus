## Why

Storia **US-104** (Sprint 9). Il progetto decide che cosa vede un Tutor: i ticket dei suoi progetti in sola lettura
(US-304), gli annunci riservati ai suoi progetti e i progetti tra cui scegliere per una nuova segnalazione. Oggi le
assegnazioni esistono solo nei dati iniziali: un Tutor creato da NEXUS (US-103) non ha progetti e non può segnalare.

## What Changes

- **Elenco degli utenti:** una colonna «Progetti» con i progetti assegnati ai Tutor; per Call Center e ADMIN
  «Tutti», perché vedono già ogni progetto.
- **Pagina «Progetti dell'utente»** (`/utenti/:id/progetti`), raggiungibile dall'elenco per ogni Tutor:
  - una casella per progetto attivo;
  - un progetto disattivato già assegnato resta in elenco e si può solo togliere;
  - il salvataggio sostituisce l'insieme dei progetti e riporta all'elenco con la conferma.
- **Regole:**
  - solo i Tutor hanno progetti assegnati;
  - un progetto disattivato non si assegna;
  - l'effetto è immediato, alla richiesta successiva del Tutor.
- **Audit:** ogni modifica, con i codici dei progetti prima e dopo; un salvataggio senza modifiche non registra nulla.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `user-management`: assegnazione dei progetti ai Tutor (si aggiunge ai requisiti di US-103).

## Impact

- Backend: `GET /api/v1/users/{id}`, `PUT /api/v1/users/{id}/projects`; i progetti nell'elenco degli utenti.
  Nessuna migrazione: la tabella `user_project` esiste dallo Sprint 1.
- Frontend: colonna «Progetti» e azione «Progetti» nella pagina «Utenti», nuova pagina `/utenti/:id/progetti`.
