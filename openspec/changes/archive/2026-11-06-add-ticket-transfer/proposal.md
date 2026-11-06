## Why

Storia **US-111** (Sprint 9, ripianificato il 06/11). Quando un Tutor esce o cambia mansione, le sue segnalazioni
aperte restano senza nessuno: oggi non c'è modo di passarle a un collega. Il PO ha deciso che un Tutor con
segnalazioni aperte non si disattiva (US-105) finché l'ADMIN non le sposta su un Tutor attivo dello stesso progetto.

## What Changes

- **Pagina «Segnalazioni di …»** (`/utenti/:id/segnalazioni`), dall'elenco degli utenti per ogni Tutor:
  - le segnalazioni aperte del Tutor, raggruppate per progetto;
  - per ogni progetto si scelgono le segnalazioni da spostare (tutte già selezionate) e il Tutor che le riceve, tra
    quelli attivi assegnati al progetto.
- **Spostamento** (decisione del PO, 06/11):
  - con la segnalazione passa al nuovo Tutor anche il beneficiario;
  - tirocinio e documenti seguono la segnalazione;
  - le segnalazioni già chiuse restano a nome del Tutor di allora.
- **Tracce:**
  - audit della segnalazione (`TRANSFER`, Tutor prima e dopo), visibile nella cronologia come «Passata a un altro
    Tutor: A → B»;
  - audit del beneficiario;
  - notifica `TICKET_TRANSFERRED` al nuovo Tutor, in app e per email.
- **Regole:** solo segnalazioni aperte, dello stesso progetto; il Tutor che le riceve è attivo, assegnato al progetto e
  diverso da quello attuale.

## Capabilities

### New Capabilities
- `ticket-transfer`: spostamento delle segnalazioni aperte da un Tutor a un altro.

### Modified Capabilities
<!-- nessuna -->

## Impact

- Backend: `GET /api/v1/users/{id}/open-tickets`, `POST /api/v1/tickets/transfer` (ADMIN; il Coordinatore arriva con
  US-112). Nessuna migrazione: il tipo di notifica è libero nel vincolo `ck_notification_type`.
- Frontend: pagina `/utenti/:id/segnalazioni`, azione «Segnalazioni» nell'elenco degli utenti, voce di cronologia e
  testo della notifica.
