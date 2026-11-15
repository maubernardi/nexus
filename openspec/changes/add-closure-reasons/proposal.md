## Why

Storia **US-1103** (Sprint 14): la relazione finale e le statistiche devono dire perché un tirocinio è finito. Se
finisce prima della data prevista, il motivo è un'interruzione voluta dall'azienda o dal destinatario; altrimenti è una
chiusura regolare (PO, 15/11). Le voci le gestisce l'ADMIN in un catalogo (PO, 15/11).

## What Changes

- **Catalogo dell'ADMIN «Motivi di chiusura del tirocinio»**, con tre voci iniziali:
  - «Chiusura regolare», che non si disattiva perché è il motivo automatico;
  - «Interruzione anticipata per volontà dell'azienda»;
  - «Interruzione anticipata per volontà del destinatario».

  Le voci si aggiungono, rinominano e disattivano come negli altri cataloghi.
- **Report di restituzione:**
  - se la fine effettiva precede la fine prevista della scheda, il Tutor sceglie il motivo tra le interruzioni attive,
    obbligatorio;
  - altrimenti il motivo è «Chiusura regolare», assegnato da NEXUS.
- Il motivo compare nel report inviato e nella cronologia della chiusura, ed entra nell'audit. US-1011 lo userà nella
  relazione finale.
- **Migrazione V41** solo additiva, con i vincoli con nome testati.

## Non-goals

- Segnaposto della relazione finale: arrivano con US-1011.
- Motivo per i report già inviati: restano senza, perché il report non si modifica.
- Proroga (US-1012): finché non c'è, la fine prevista è quella della scheda.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `reference-data`: requisito aggiunto per il catalogo dei motivi di chiusura del tirocinio.
- `ticket-lifecycle`: requisito aggiunto per il motivo di chiusura nel report di restituzione.

## Impact

- Migrazione **V41**; nessun passo sul server.
- API: `ClosureReportFormDTO.closureReasonId`, `ClosureReportDTO.closureReason`; il catalogo `closure-reasons` in
  `/catalogs`; `GET /reference/interruption-reasons` (voci attive di interruzione).
- Frontend: sezione nei Cataloghi; campo del motivo nel report di restituzione e riga nel report inviato.
  Accessibilità: WCAG 2.2 AA (3.3.1, 3.3.2, 4.1.3); test axe. PWA: nessun impatto.
- Collaudo: nuova prova T-24.1.
