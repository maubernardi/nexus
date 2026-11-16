## Why

Storia **US-1012** (Sprint 14): un tirocinio si può prorogare. La proroga è una nuova convenzione, con il suo numero e
le sue date, mentre tutti gli altri dati restano in comune (PO, 15/11). Oggi la scheda ha una sola convenzione, con le
date del tirocinio.

## What Changes

- **Proroghe nella scheda del tirocinio**, solo a tirocinio avviato e non ancora chiuso. Ogni proroga ha:
  - numero, data e data di stipula della nuova convenzione;
  - data di inizio, successiva alla fine della convenzione precedente ma non per forza il giorno dopo (PO, 15/11);
  - data di fine.

  Le proroghe possono essere più di una, numerate 1, 2, 3…
- **Ogni convenzione ha i suoi progetti formativi**, numerati al suo interno e dentro le sue date, proroghe comprese
  (PO, 15/11).
- **Fine prevista del tirocinio** = fine dell'ultima convenzione. La scheda la mostra, il report di restituzione la
  usa per riconoscere l'interruzione anticipata (US-1103), e il monte ore del tirocinio conta solo i giorni coperti da
  una convenzione (PO, 15/11).
- **Audit** delle proroghe e dei loro progetti; azzeramento con l'appuntamento fallito, come la convenzione.
- **Migrazione V42** solo additiva; vincoli con nome, testati.

## Non-goals

- **Documenti** della proroga, cioè la convenzione e i progetti formativi su modello: arrivano con US-1011, che usa gli
  stessi modelli (PO, 15/11).
- **Progetti formativi obbligatori** in una proroga: non sono richiesti, come per la convenzione iniziale fino
  all'avvio.
- **Proroghe dopo la chiusura del tirocinio** o prima dell'avvio.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `internship-record`: requisiti aggiunti per le proroghe e per la fine prevista con le proroghe.
- `ticket-lifecycle`: requisito aggiunto per la fine prevista del report di restituzione con le proroghe.

## Impact

- Migrazione **V42**; nessun passo sul server.
- API:
  - `InternshipFormDTO.extensions`, ciascuna con i suoi `trainingProjects`;
  - `InternshipDTO.extensions`, `currentEndDate` ed `extensible`.
- Frontend: sezione «Proroghe» nella scheda del tirocinio, con le proroghe e i loro progetti formativi; la fine
  prevista con le proroghe nella convenzione e nel report di restituzione. Accessibilità: WCAG 2.2 AA (1.3.1, 2.4.6,
  3.3.1, 3.3.2); test axe. PWA: nessun impatto.
- Collaudo: nuova prova T-24.2.
