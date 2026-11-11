## Why

Storia **US-1009** (Sprint 13): la relazione finale dei documenti reali (US-1011) riporta la fine **effettiva** del
tirocinio («Al»), che può essere diversa da quella prevista, e il progetto formativo indica per ogni tutor aziendale la
sua **funzione o ruolo** (planning del 09/11). Oggi NEXUS non ha nessuno dei due dati.

## What Changes

- **Report di restituzione** (US-1101): nuovo campo **data di fine effettiva del tirocinio**, obbligatorio,
  precompilato con la fine prevista della scheda, non prima della data di inizio. Si salva con il report e, come il
  report, non si modifica dopo l'invio; compare nel report inviato e nell'audit della chiusura con il valore.
- La fine effettiva **vale solo per la relazione finale**: la scheda del tirocinio (inizio e fine prevista) e i
  progetti formativi restano com'erano (PO, 09/11).
- **Scheda del tirocinio**: per ogni tutor aziendale la **funzione o ruolo**, testo libero facoltativo (es.
  «Responsabile di produzione»). Nell'audit, come gli altri dati dei tutor, solo «cambiato».
- **Documenti di oggi**: la riga dei tutor aziendali riporta anche il ruolo («Mario Rossi, Responsabile di produzione
  (055 123456)»); i segnaposto separati arrivano con US-1011.
- **Migrazione V38** solo additiva; vincolo con nome sul ruolo, testato.

## Non-goals

- Segnaposto nuovi e relazione finale scaricabile: arrivano con US-1011.
- Data di fine effettiva per i report già inviati: restano senza (il report non si modifica), e nella relazione finale
  si scriverà a mano.
- Proroga del tirocinio o modifica della fine prevista dalla chiusura: la scheda si corregge come oggi.
- Ruolo obbligatorio o scelto da un catalogo: è testo libero facoltativo (PO, 09/11).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: requisito aggiunto per la data di fine effettiva nel report di restituzione.
- `internship-record`: requisito aggiunto per la funzione o ruolo dei tutor aziendali.
- `internship-documents`: requisito aggiunto per il ruolo nella riga dei tutor aziendali.

## Impact

- Migrazione **V38**; nessun passo sul server.
- API: `ClosureReportFormDTO` con `actualEndDate` obbligatoria e `ClosureReportDTO` con `actualEndDate` (assente per i
  report di prima); `CompanyTutorDTO` con `role`.
- Frontend: pagina del report di restituzione (campo data precompilato dalla scheda), vista del report inviato, sezione
  dei tutor aziendali della scheda. Accessibilità: WCAG 2.2 AA (3.3.1 identificazione degli errori, 3.3.2 etichette o
  istruzioni, 1.3.1 informazioni e relazioni); test axe. PWA: nessun impatto (le API non vanno in cache).
- Collaudo: nuova prova T-23.2.
