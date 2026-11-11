## 1. Backend

- [x] 1.1 Migrazione V39 solo additiva: convenzione su `internship`, tabella `internship_training_project` con vincoli
  ed esclusione delle sovrapposizioni; vincoli testati per nome
- [x] 1.2 Mesi arrotondati (`InternshipMonths`) con test unitari
- [x] 1.3 Scheda: convenzione e progetti nel salvataggio (controlli sulle date, ordine, sovrapposizioni), mesi e monte
  ore in uscita, mansione proposta, audit; test di integrazione
- [x] 1.4 Appuntamento fallito: azzeramento di convenzione e progetti; test

## 2. Frontend

- [x] 2.1 Sezioni «Convenzione» e «Progetti formativi» (`TrainingProjectsFields`): righe ripetibili precompilate,
  controlli nel browser, errori dal server sui campi, durata e monte ore dei progetti salvati; test con axe

## 3. Consegna

- [x] 3.1 Verifiche: `./mvnw verify`; lint, formato, test e build del frontend
- [x] 3.2 Controllo in Chrome prima della PR (A26): tastiera, axe, tema scuro, smartphone
- [x] 3.3 Collaudo (T-23.3) e PR con la CI verde
