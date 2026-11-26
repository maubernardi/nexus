## 1. Database e backend

- [x] 1.1 `V54__closure_report_company_review.sql`: voti, nota e `company_id` su `closure_report`, con i vincoli;
      `DomainConstraintsIT`
- [x] 1.2 `ClosureReport`, form e DTO del report con i voti e la nota; `ClosureReportServiceImpl` con l'azienda del
      tirocinio e l'audit dei voti
- [x] 1.3 `GET /api/v1/companies/{id}/reviews` a pagine con la media; media nella lista del Tutor;
      `GET /api/v1/companies/directory/{id}`
- [x] 1.4 Test: report con la valutazione (`400` sui voti, audit senza la nota), valutazioni dell'azienda (media,
      ordine, pagine, nome del Tutor, nessun dato del beneficiario, ruoli), lista e scheda del Tutor

## 2. Frontend

- [x] 2.1 Atomo `ReviewFace` (faccina e media, testo per il lettore di schermo)
- [x] 2.2 Report: tre voti con le faccine, media dal vivo, nota se la faccina non è la migliore; report inviato con
      la valutazione; test
- [x] 2.3 Organismo `CompanyReviewsSection` nella scheda del Call Center; test con axe
- [x] 2.4 `TutorCompanyPage` su `/aziende/:id` per il Tutor; colonna della media e collegamento nella lista; test

## 3. Dati dimostrativi e prove

- [x] 3.1 Seed: due tirocini chiusi con la valutazione e una segnalazione in tirocinio di `tutor1`
- [x] 3.2 Prova in Chrome in locale (A26), il 26/11, con i dati del seed:
      - con Chrome senza finestra: valutazioni nella scheda del Call Center, report con le faccine e la media, lista e
        scheda del Tutor, anche in tema scuro;
      - con l'estensione: report inviato davvero sul tirocinio del seed, con la valutazione nella vista del report
- [x] 3.3 Collaudo T-38 in `docs/test/collaudo.md`
- [x] 3.4 `./mvnw verify` (249 test unitari, 592 di integrazione); `pnpm lint`, `format:check`, `test --run`
      (362), `build`
