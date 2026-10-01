## Why

Storie **EN-1** (motore delle transizioni) e **US-301** (nuova segnalazione), Sprint 1. La segnalazione è il cuore di
NEXUS: ogni fase 1–7 del brief cambia lo stato del ticket. Serve un unico punto che applichi le regole di transizione,
registri la cronologia e gestisca le modifiche concorrenti, così che le storie successive aggiungano solo le proprie
transizioni. La prima a usarlo è l'invio della segnalazione da parte del Tutor.

## What Changes

- **Motore delle transizioni**: catalogo delle transizioni ammesse (stati di partenza, stato di arrivo, ruoli), un
  servizio che le applica verificando stato, ruolo e versione attesa del ticket, e scrive una riga di
  `ticket_status_history` per ogni cambio (autore e nota). Transizione non ammessa o versione superata → `409`, ruolo non
  autorizzato → `403`; modifiche concorrenti rilevate dal blocco ottimistico → `409`.
- API `POST /api/v1/tickets` (solo Tutor): segnalazione NORMAL di un proprio candidato, per un progetto a cui il Tutor è
  assegnato, con una tipologia di mansione attiva del catalogo. Il ticket nasce in `NUOVA` con numero progressivo.
- API `GET /api/v1/me/projects`: progetti attivi assegnati all'utente corrente.
- Pagina **Nuova segnalazione** per il Tutor (candidato, progetto, mansione), raggiungibile dal menu, dalla Home e dalla
  conferma di registrazione del candidato (candidato preselezionato).

## Capabilities

### New Capabilities
- `ticket-lifecycle`: regole comuni dei cambi di stato del ticket (transizioni ammesse, cronologia, concorrenza).
- `ticket-submission`: invio della segnalazione normale da parte del Tutor.

### Modified Capabilities
<!-- nessuna -->

## Non-goals

- Mansione a testo libero e approvazione ADMIN (US-302), segnalazione speciale dalla bacheca (US-306).
- Elenco, dettaglio e cronologia dei ticket nell'interfaccia (US-303, US-305); coda del Call Center (US-401, change
  `add-cc-queue`); presa in carico (US-402).
- Regola "un solo ticket aperto per candidato": non prevista dal brief, registrata come domanda aperta Q12.
