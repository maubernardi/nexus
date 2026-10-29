## Why

Storia **US-1101** (Sprint 8). Il percorso si chiude con il report di restituzione: il Tutor dice com'è andato il
tirocinio e se il beneficiario è stato assunto. Con il report la segnalazione arriva in `FORM_RESTITUZIONE`, l'ultima
fase, e il beneficiario può avere una nuova segnalazione.

## What Changes

- **Migrazione V26**: tabella `closure_report`, una per segnalazione, in sola aggiunta (il database rifiuta modifiche
  e cancellazioni, come per le note dell'azienda). Campi:
  - soddisfazione dell'azienda e del beneficiario da 1 a 5;
  - obiettivi raggiunti e difficoltà riscontrate (testi facoltativi);
  - assunto sì/no: sì con la formula di assunzione, no con il motivo (cataloghi di US-109);
  - mansione ancora disponibile sì/no.
- **Transizione `CLOSE`** `IN_TIROCINIO` → `FORM_RESTITUZIONE`, solo per il Tutor della segnalazione:
  `POST /tickets/{id}/closure-report`. Cronologia, audit con le scelte (senza i testi liberi) e notifica
  `CLOSURE_REPORT_SUBMITTED` all'operatore che ha in carico la segnalazione.
- **Dettaglio della segnalazione**: il report inviato, in sola lettura, per chi vede la segnalazione.
- **Frontend**: pagina "Report di restituzione" per il Tutor (dal dettaglio, durante il tirocinio), con conferma
  prima dell'invio; il report inviato si legge nel dettaglio.

La mansione che torna libera e l'esito nello storico dell'azienda sono di US-1102.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: report di restituzione e chiusura del percorso.
