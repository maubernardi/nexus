## 1. Backend

- [x] 1.1 Migrazione V42 solo additiva: `internship_extension` con vincoli ed esclusione, `internship_training_project.extension_id`; vincoli testati per nome
- [x] 1.2 Scheda: proroghe nel salvataggio (stato, date, progetti per convenzione), fine prevista con le proroghe, monte ore sui periodi, audit, azzeramento; test di integrazione
- [x] 1.3 Report di restituzione con la fine prevista delle proroghe; test

## 2. Frontend

- [x] 2.1 Sezione «Proroghe» con i loro progetti formativi, proposte del pulsante, controlli nel browser, errori dal server; fine prevista con le proroghe; test con axe
- [x] 2.2 Report di restituzione con la fine prevista delle proroghe; test

## 3. Consegna

- [x] 3.1 Verifiche: `./mvnw verify`; lint, formato, test e build del frontend
- [x] 3.2 Controllo in Chrome prima della PR (A26, A29): tastiera, axe, tema scuro, smartphone
- [x] 3.3 Collaudo (T-24.2) e PR con la CI verde
