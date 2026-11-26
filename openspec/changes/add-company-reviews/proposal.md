## Why

Storia **US-1104** (Sprint 18), chiesta dal PO il 25/11 e precisata il 26/11. Chi propone di nuovo un'azienda, operatore
o Tutor, oggi non conosce la valutazione dei Tutor che hanno seguito i tirocini già fatti lì: l'esperienza resta nella testa del Tutor
che ha seguito il tirocinio. Il PO vuole una valutazione alla chiusura del tirocinio, visibile nella scheda
dell'azienda, con il nome del Tutor che l'ha data, così chi ricontatta l'azienda sa a chi chiedere che cosa non è
andato.

## What Changes

- **Nel report di restituzione** (US-1101) tre voti da 1 a 5, obbligatori, ognuno con cinque faccine:
  - soddisfazione nell'interazione con l'azienda;
  - capacità dell'azienda di accogliere il beneficiario;
  - capacità formativa dell'azienda e del tutor aziendale.

  Sono diversi dalla «Soddisfazione dell'azienda» del report, che resta.
- Mentre il Tutor sceglie, la **media** dei tre voti compare come faccina: quella del numero intero più vicino.
- La **nota** c'è sempre, anche con 😄 (PO, 26/11, dal collaudo): una sola per i tre voti, al massimo 500 caratteri,
  facoltativa, senza dati personali.
- **Scheda dell'azienda** del Call Center: sezione «Valutazioni dei tirocini». Contiene la media delle medie come faccina,
  con il numero di tirocini valutati, e l'elenco a pagine, dalla più recente: data di fine del tirocinio, faccina, nota
  e nome del Tutor. Il beneficiario non compare.
- **Tutor**:
  - nella sua lista delle aziende (US-511) una colonna con la faccina media;
  - il nome dell'azienda apre una scheda in sola lettura, con i dati della lista e le valutazioni.
- Il report inviato mostra anche la valutazione; non si modifica, come il resto. I voti entrano nell'audit, la nota no.
- **Dati finti del collaudo**: due tirocini già chiusi con la valutazione e un tirocinio in corso da chiudere.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: valutazione dell'azienda nel report di restituzione.
- `company-registry`: valutazioni dei tirocini nella scheda dell'azienda; scheda in sola lettura per il Tutor; media
  nella lista del Tutor.
- `dev-seed-data`: tirocini dimostrativi con le valutazioni.

## Non-goals

- Valutazioni per i report già inviati: restano senza.
- Modifica o cancellazione di una valutazione: il report non si modifica dopo l'invio.
- La media nell'elenco del Call Center: la vede nella scheda.
- Il 🚢 della nave scuola (US-515).

## Impact

- Database: **V54** su `closure_report`:
  - i tre voti con i `CHECK`, tutti o nessuno;
  - la nota;
  - `company_id`, l'azienda del tirocinio al momento della chiusura, con FK e indice.

  La tabella resta immodificabile dopo l'invio: la V54 aggiunge solo colonne.
- Backend:
  - `ClosureReport`, `ClosureReportFormDTO`, `ClosureReportDTO`, `ClosureReportServiceImpl` con l'audit;
  - `GET /api/v1/companies/{id}/reviews` (Call Center, ADMIN e Tutor), a pagine, con la media;
  - `GET /api/v1/companies/directory/{id}` per la scheda del Tutor;
  - la media nell'elenco del Tutor.
- Frontend:
  - atomo `ReviewFace` (faccina e media) e campo con le faccine nel report;
  - organismo `CompanyReviewsSection`;
  - `TutorCompanyPage` su `/aziende/:id`, scelta in base al ruolo;
  - colonna e collegamento nella lista del Tutor;
  - testi in `it.ts`.
- Accessibilità (WCAG 2.2 AA):
  - i voti sono pulsanti di opzione nativi, con la faccina e una parola («Buona»);
  - la media ha il testo per il lettore di schermo («Media 4,3 su 5: buona»);
  - le faccine sono icone, non emoji.
- PWA: nessuna API in cache.
- Collaudo: prove T-38 sul PC del PO prima del merge (EN-11).
