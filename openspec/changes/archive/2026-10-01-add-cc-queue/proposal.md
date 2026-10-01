## Why

Storia **US-401** (Sprint 1): l'operatore del Call Center deve vedere le segnalazioni da lavorare, prima le speciali
(fast-track) e poi in ordine di arrivo, per lavorare in modo equo e rapido. Chiude l'obiettivo dello sprint: la
segnalazione inviata dal Tutor (US-301) compare nella coda.

## What Changes

- API `GET /api/v1/tickets/queue` (Call Center, ADMIN): ticket in `NUOVA` o `IN_LAVORAZIONE` non ancora assegnati a un
  operatore, prima i fast-track e poi dal più vecchio; filtri facoltativi per progetto e zona di residenza del beneficiario.
- API `GET /api/v1/reference/projects`: progetti attivi (il Call Center non è assegnato a progetti, ma deve filtrarli).
- Pagina **Coda segnalazioni** per Call Center e ADMIN: tabella dati accessibile ottimizzata per desktop e usabile a
  320 px, badge testuale "Fast-track", filtri che aggiornano l'elenco e ne annunciano il numero, filtri nell'indirizzo
  (link condivisibile, tasto Indietro). Voce di menu e azione in Home.

## Capabilities

### New Capabilities
- `cc-queue`: coda di lavoro del Call Center.

### Modified Capabilities
<!-- nessuna: l'elenco dei progetti si aggiunge ai dati di riferimento quando questa change e add-beneficiary-registration
     sono archiviate; qui è descritto come requisito della coda -->

## Non-goals

- Presa in carico (US-402), approvazione e rifiuto del fast-track (US-403, US-404), dettaglio del ticket (US-305).
- Paginazione: la coda attesa è di decine di voci; si introdurrà se i volumi reali lo richiedono.
