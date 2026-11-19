## 1. Backend

- [x] 1.1 Migrazione V41 solo additiva: catalogo `closure_reason` con le voci iniziali, `closure_report.closure_reason_id`; vincoli testati per nome
- [x] 1.2 Catalogo nei Cataloghi (`closure-reasons`), chiusura regolare non disattivabile, voci di interruzione per il Tutor; test
- [x] 1.3 Report di restituzione: motivo obbligatorio con l'interruzione anticipata, chiusura regolare automatica; report inviato, cronologia, audit; test di integrazione

## 2. Frontend

- [x] 2.1 Sezione «Motivi di chiusura del tirocinio» nei Cataloghi
- [x] 2.2 Report di restituzione: tendina del motivo quando la fine effettiva precede la prevista; riga nel report inviato; test con axe

## 3. Consegna

- [x] 3.1 Verifiche: `./mvnw verify`; lint, formato, test e build del frontend
- [x] 3.2 Controllo in Chrome prima della PR (A26, A29): tastiera, axe, tema scuro, smartphone
- [x] 3.3 Collaudo (T-24.1) e PR con la CI verde
