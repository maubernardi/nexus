## Why

Storia **US-404** (Sprint 7): il Call Center rifiuta la corsia veloce di una segnalazione speciale e la riporta a una
lavorazione normale. Il tipo resta SPECIAL (Q1), l'annuncio resta pubblicato e torna candidabile (review dello
Sprint 6, §8.1). Il rifiuto richiede un motivo ed è registrato (EN-2).

Dalla stessa review (§8.1, interpretazione del team): se l'annuncio esce dalla bacheca mentre la candidatura aspetta,
la segnalazione perde la corsia veloce con il motivo e il Tutor è avvisato.

## What Changes

- Nuove transizioni (in lavorazione → in lavorazione, Call Center e ADMIN):
  - `FAST_TRACK_REJECT`, il rifiuto con il motivo;
  - `FAST_TRACK_DROP`, la corsia veloce che decade quando l'annuncio si archivia.
- `POST /api/v1/tickets/{id}/fast-track/reject` (`version`, `reason` obbligatorio fino a 500 caratteri):
  - servono la presa in carico e la corsia veloce in attesa;
  - la corsia veloce si spegne, l'annuncio resta pubblicato, il Tutor riceve `FAST_TRACK_REJECTED`.
- Annuncio archiviato con una candidatura in attesa: la corsia veloce decade con il motivo e il Tutor riceve
  `FAST_TRACK_DROPPED`. Vale per l'archiviazione a mano ("Annuncio #N archiviato dal Call Center") e per quella
  automatica: mansione abbinata o ritirata, azienda disattivata.
- Nuovo servizio `FastTrackService`, usato sia dalle segnalazioni sia dalla bacheca senza dipendenze circolari.
- Frontend:
  - nella sezione **Corsia veloce**, "Rifiuta la corsia veloce" con il motivo obbligatorio; dopo, la ricerca delle
    compatibili come per le altre segnalazioni;
  - nella conferma di archiviazione di un annuncio, l'avviso che la candidatura in attesa perderà la corsia veloce;
  - testi delle notifiche e voci di cronologia.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `job-board`: rifiuto della corsia veloce e corsia veloce che decade con l'annuncio.
