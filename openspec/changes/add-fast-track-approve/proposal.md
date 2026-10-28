## Why

Storia **US-403** (Sprint 7): il Call Center approva una segnalazione speciale (US-306) e la trasforma subito in
proposta all'azienda dell'annuncio. Il brief chiede un'unica operazione: segnalazione in `PROPOSTA_AZIENDA`, mansione
dell'annuncio `BLOCCATA`, annuncio `ARCHIVED`.

## What Changes

- Nuova transizione `FAST_TRACK_APPROVE` (`IN_LAVORAZIONE` → `PROPOSTA_AZIENDA`, Call Center e ADMIN).
- `POST /api/v1/tickets/{id}/fast-track/approve` (`version`):
  - servono la presa in carico e la corsia veloce in attesa;
  - l'annuncio dev'essere pubblicato e la mansione libera, a disposizione, di un'azienda attiva non esclusa;
  - nella stessa transazione: la mansione dell'annuncio diventa la proposta e si blocca, l'annuncio si archivia
    ("Corsia veloce approvata: segnalazione n. N"), l'evento entra nello storico dell'azienda, il Tutor riceve la
    notifica della proposta;
  - la corsia veloce è consumata (`fastTrack` → `false`); il tipo SPECIAL resta come traccia.
- L'abbinamento normale e l'approvazione condividono la stessa logica (`proposeSlot` / `afterProposal`).
- Con la corsia veloce in attesa l'abbinamento normale è rifiutato (`409`): prima si decide sulla corsia veloce.
- Cronologia: "Corsia veloce approvata: proposta all'azienda", con mansione e azienda. Una presa in carico che non
  cambia lo stato (speciale già in lavorazione) non lascia più un "Cambio di stato" vuoto.
- Frontend: nel dettaglio della segnalazione il Call Center trova la sezione **Corsia veloce**. Mostra la mansione e
  l'azienda dell'annuncio, lette dalla gestione della bacheca, e, presa in carico la segnalazione, il comando "Approva
  la corsia veloce" con conferma ed esito. La ricerca delle compatibili non compare finché la corsia veloce aspetta.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-board`: approvazione della corsia veloce.
