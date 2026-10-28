## Why

Storia **US-306** (Sprint 7): il Tutor candida un suo beneficiario direttamente a un annuncio #N della bacheca, per
ottenere una corsia prioritaria (fast-track) presso il Call Center. Decisioni del PO, dalla review dello Sprint 6:
- §8.1: **una sola candidatura per annuncio**, la prima blocca l'annuncio finché il Call Center non decide;
- §8.2: il pulsante "Candida un beneficiario" in bacheca; la segnalazione ha la tipologia della mansione
  dell'annuncio.

## What Changes

- **Migrazione V19**: indice unico parziale `uq_ticket_fast_track_board_post`, al più una segnalazione con la corsia
  veloce per annuncio.
- Nuova transizione `SUBMIT_SPECIAL` (nome `SUBMIT`, da nessuno stato a `IN_LAVORAZIONE`, ruolo Tutor). L'audit di
  creazione registra anche `fastTrack` e `boardPostId`.
- `POST /api/v1/tickets/special` (`boardPostId`, `beneficiaryId`, `projectId`), solo per il Tutor:
  - annuncio pubblicato, per tutti o riservato a un suo progetto (bozze e altri progetti: `404`; archiviato: `409`);
  - beneficiario suo, senza segnalazioni aperte;
  - annuncio non bloccato da un'altra candidatura (`409`);
  - progetto: quello dell'annuncio se riservato, altrimenti uno dei progetti del Tutor;
  - la segnalazione nasce SPECIAL, con la corsia veloce, in lavorazione, collegata all'annuncio e con la tipologia
    della mansione.
- `GET /api/v1/board/{id}` per il Tutor. Nella bacheca del Tutor ogni annuncio dice se ha una candidatura in
  valutazione (`candidatureInProgress`). La gestione del Call Center indica la segnalazione che blocca l'annuncio. Il
  dettaglio della segnalazione indica l'annuncio. In cronologia: "Candidatura all'annuncio #N della bacheca".
- Frontend:
  - in bacheca "Candida un beneficiario", oppure "Candidatura in valutazione";
  - pagina `/bacheca/:id/candidatura` con la scheda dell'annuncio, il beneficiario e, per un annuncio aperto a tutti,
    il progetto; esito con il link alla segnalazione;
  - "Candidatura dalla bacheca" nel dettaglio della segnalazione;
  - riga "Candidatura in valutazione" nella scheda di gestione.
- Test: `WorkflowScenarios`, preparazione comune per i test del flusso (azione A7).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-board`: candidatura a un annuncio.
