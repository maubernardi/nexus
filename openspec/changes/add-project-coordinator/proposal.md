## Why

Storia **US-112** (Sprint 9, richiesta del PO del 06/11). Oggi solo l'ADMIN sposta le segnalazioni tra i Tutor
(US-111). Il PO vuole che per ogni progetto ci sia un **Coordinatore**: un Tutor con un permesso in più, che riorganizza
il lavoro del suo progetto senza passare dall'ADMIN.

## What Changes

- **Coordinatore per progetto** (decisione del PO, 06/11):
  - nella pagina dei progetti di un Tutor, l'ADMIN segna per ogni progetto assegnato se il Tutor lo coordina;
  - lo stesso Tutor può coordinare un progetto ed essere solo Tutor di un altro;
  - un progetto può avere più Coordinatori; nessun nuovo ruolo in Keycloak.
- **Coordinamento** (`/coordinamento`, voce di menu solo per chi coordina almeno un progetto):
  - per ogni progetto coordinato, tutte le segnalazioni aperte del progetto, con il loro Tutor;
  - il Coordinatore ne sceglie alcune e il Tutor che le riceve, tra quelli attivi del progetto, sé compreso;
  - lo spostamento è quello di US-111: con la segnalazione passa il beneficiario, con audit, cronologia e notifica.
- **Regole:**
  - il Coordinatore sposta solo nei progetti che coordina, altrimenti `403`;
  - si coordina solo un progetto assegnato: togliendo il progetto si toglie anche il coordinamento;
  - chi smette di essere Tutor (cambio di ruolo, US-105) perde il coordinamento;
  - audit dell'utente (`PROJECTS_CHANGED` con i progetti coordinati prima e dopo).
- Nell'elenco degli utenti, accanto al progetto compare «(Coordinatore)».

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-transfer` (introdotta da `add-ticket-transfer`, non ancora archiviata): requisito aggiunto per il Coordinatore.

## Impact

- **Migrazione V29**: colonna `coordinator` in `user_project`, `false` per le assegnazioni esistenti.
- Backend:
  - `PUT /api/v1/users/{id}/projects` accetta `coordinatedProjectIds`; `UserDTO` espone `coordinatedProjects`;
  - `GET /api/v1/me` espone `coordinator`;
  - nuovo `GET /api/v1/me/coordination` (Tutor);
  - `POST /api/v1/tickets/transfer` aperto ai Tutor, con il controllo del progetto.
- Frontend:
  - casella «Coordinatore» nella pagina dei progetti;
  - pagina `/coordinamento` e voce di menu;
  - il modulo di spostamento diventa l'organismo condiviso `TransferGroupForm`.
