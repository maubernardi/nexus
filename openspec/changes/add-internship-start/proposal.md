## Why

Storia **US-1003** (Sprint 8): il Tutor conferma l'avvio del tirocinio. La segnalazione passa in `IN_TIROCINIO`
solo con la scheda completa, ma la scheda resta modificabile (PO, 29/10). I dati richiesti li ha decisi il PO nella
review dello Sprint 7 (§8.2).

## What Changes

- **Transizione `START`** (`APPUNTAMENTO` → `IN_TIROCINIO`), del solo Tutor della segnalazione, con la versione
  letta: `POST /tickets/{id}/start`.
- **Scheda completa** (decisione §8.2). Servono:
  - tipo di tirocinio, data di inizio, data di fine prevista, sede, codice ATECO;
  - almeno un tutor aziendale e la griglia con almeno un giorno;
  - codice fiscale, data e luogo di nascita, residenza del beneficiario;
  - il legale rappresentante dell'azienda con codice fiscale, nascita e residenza.

  Sospensioni e note restano facoltative. La scheda dice che cosa manca (`missingForStart`, sui dati salvati);
  l'avvio con la scheda incompleta è rifiutato (`409`) con l'elenco.
- **All'avvio**:
  - cronologia e audit (`START`);
  - evento `TIROCINIO_AVVIATO` nello storico dell'azienda, già contato dal riepilogo di US-503;
  - notifica `INTERNSHIP_STARTED` all'operatore che ha in carico la segnalazione.
- **Frontend**, nella pagina della scheda una sezione "Avvio del tirocinio":
  - se manca qualcosa, l'elenco con un collegamento a ogni campo;
  - se la scheda è completa, "Avvia il tirocinio" con la conferma;
  - dopo l'avvio, una riga ricorda che la scheda resta modificabile.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: avvio del tirocinio.
- `internship-record`: completezza della scheda per l'avvio.
