## Why

Storia **US-702** (Sprint 5): il Tutor deve poter rifiutare la proposta per il suo beneficiario, spiegando il motivo,
per tornare alla ricerca di un'altra mansione. Decisione del PO (Q3, 01/10): motivo obbligatorio, segnalazione al Call
Center in `RIAPERTO`, mansione libera, **azienda esclusa** per quella segnalazione.

## What Changes

- Transizione `PROPOSAL_REJECT`: `PROPOSTA_AZIENDA` → `RIAPERTO`, solo per il **Tutor della segnalazione**, prima
  dell'accettazione (`POST /api/v1/tickets/{id}/reject`, motivo obbligatorio).
- Effetti: mansione **libera** (audit `RELEASE`); **azienda esclusa** per la segnalazione con il motivo (non compare più
  tra le compatibili); la segnalazione resta allo stesso operatore, in cima a "In carico a me" (US-802).
- Tracciamento: cronologia "Proposta rifiutata dal Tutor – «azienda» esclusa per questa segnalazione" con il motivo;
  evento `PROPOSTA_RIFIUTATA` e l'esclusione nello storico dell'azienda; notifica `PROPOSAL_REJECTED` all'operatore.
- Dettaglio: "Rifiuta la proposta" accanto ad "Accetta la proposta", con il motivo.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: rifiuto della proposta.
