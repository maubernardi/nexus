## Why

Storia **US-303** (Sprint 3), riscritta con la nota operativa 1 del PO (24/10): "sia per tutor che per operatori, la
landing page deve essere l'elenco delle segnalazioni"; nota 4: per l'operatore il filtro per zona non serve, serve per
stato. Assorbe US-304 (segnalazioni del proprio progetto) e la vista "Tutte" di US-405.

## What Changes

- **API unica** `GET /api/v1/tickets?view=&status=&projectId=` (sostituisce `/tickets/queue` e `/tickets/assigned-to-me`):
  - Tutor: `MINE` (predefinita, le proprie, dalla più recente) e `PROJECT` (dei progetti a cui è assegnato, anche dei
    colleghi, complete — Q11; mai di altri progetti);
  - Call Center/ADMIN: `TO_TAKE` (predefinita: la coda, fast-track e poi per arrivo), `ASSIGNED_TO_ME` (aperte in carico,
    dalla più vecchia), `ALL` (tutte, dalla più recente, con l'operatore in carico);
  - filtri per stato e progetto; vista non consentita al ruolo → `400`. Query con Specification e fetch join.
- **Home "Segnalazioni"** per tutti i ruoli: viste come link con `aria-current`, filtri nell'indirizzo (`?vista=`,
  `?stato=`, `?progetto=`), conteggio annunciato, tabella con colonne per vista, presa in carico dalla vista "Da
  prendere in carico", "Nuova segnalazione" per il Tutor.
- Spariscono le pagine "Coda" e "Le mie lavorazioni" e la Home di benvenuto; `/coda` e `/lavorazioni` reindirizzano alle
  viste corrispondenti. Nel menu la voce "Home" diventa "Segnalazioni".
- Nessun filtro per zona nell'elenco (resta nella ricerca delle mansioni compatibili).

## Capabilities

### New Capabilities
- `ticket-list`: elenco delle segnalazioni come Home, con viste per ruolo.

### Modified Capabilities
- `cc-queue`: coda come vista della Home, filtri per stato e progetto.

## Non-goals

- Dettaglio e cronologia della segnalazione per il Tutor (US-305): per ora il numero è un link solo per il Call Center
  (lavorazione). Paginazione dell'elenco (volumi attuali modesti).
