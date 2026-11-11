## 1. Backend

- [x] 1.1 Migrazione V38 solo additiva: `closure_report.actual_end_date` e `internship_company_tutor.role` con
  `ck_internship_company_tutor_role`; vincolo testato per nome
- [x] 1.2 Report di restituzione: data di fine effettiva obbligatoria, non prima dell'inizio della scheda; salvata con
  il report, nell'audit della chiusura e nel report inviato; test di integrazione (anche: la scheda non cambia)
- [x] 1.3 Scheda del tirocinio: ruolo dei tutor aziendali (spazi = vuoto), audit senza valori; test di integrazione
- [x] 1.4 Documenti: riga dei tutor «Nome, ruolo (recapito)» senza separatori vuoti; test

## 2. Frontend

- [x] 2.1 Report di restituzione: campo data precompilato con la fine prevista della scheda, controllo «non prima
  dell'inizio», errori dal server sul campo; vista del report inviato con la data; test con axe
- [x] 2.2 Scheda del tirocinio: campo del ruolo per ogni tutor aziendale; test con axe

## 3. Consegna

- [x] 3.1 Verifiche: `./mvnw verify`; lint, formato, test e build del frontend
- [x] 3.2 Controllo in Chrome prima della PR (A26): tastiera, axe, tema scuro, smartphone
- [x] 3.3 Collaudo (T-23.2) e PR con la CI verde
