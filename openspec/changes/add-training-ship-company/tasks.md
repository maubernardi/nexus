## 1. Database e backend

- [x] 1.1 `V55__training_ship.sql`: `company.training_ship`, `closure_report.training_ship_proposed`,
      `notification.company_id` con FK; `DomainConstraintsIT`
- [x] 1.2 Azienda: entità, DTO, modulo, audit, filtro `trainingShip`; elenco e scheda del Tutor; `JobSlotMatchDTO`
- [x] 1.3 Report: risposta nella form, nel DTO e nell'audit; con il sì, avviso all'operatore e nota nel diario
- [x] 1.4 Avvisi: `TRAINING_SHIP_PROPOSED` con i testi predefiniti; notifica ed email con l'azienda
- [x] 1.5 Test: azienda (`CompanyResourceIT`), report (`ClosureReportIT`), avvisi ed email, abbinamento e bacheca

## 2. Frontend

- [x] 2.1 Atomo `TrainingShipBadge`
- [x] 2.2 Casella nel modulo dell'azienda; filtro e simbolo nell'elenco; lista e scheda del Tutor
- [x] 2.3 Simbolo nell'abbinamento, nella proposta e nella bacheca del Call Center
- [x] 2.4 Domanda nel report e risposta nella vista; avviso che apre la scheda dell'azienda; testi del messaggio
- [x] 2.5 Test con axe

## 3. Dati dimostrativi e prove

- [x] 3.1 Seed: «Demo Ristorazione S.n.c.» nave scuola
- [x] 3.2 Prova in Chrome in locale (A26)
- [x] 3.3 Collaudo T-39 in `docs/test/collaudo.md`
- [x] 3.4 `./mvnw verify`; `pnpm lint`, `format:check`, `test --run`, `build`
