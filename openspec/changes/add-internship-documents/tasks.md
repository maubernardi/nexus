## 1. Backend

- [x] 1.1 Dipendenza `poi-ooxml` 6.0.0; `DocxTemplate` (segnaposto anche spezzati, tabelle, intestazioni, a capo) con test unitari
- [x] 1.2 Modelli segnaposto della Convenzione (uno per tipo e il predefinito) e lo script che li genera
- [x] 1.3 `InternshipDocumentService` e `GET /tickets/{id}/internship/documents/convenzione`, audit `DOCUMENT_GENERATED`
- [x] 1.4 IT: documento compilato e riaperto, modello per tipo e predefinito, dati mancanti, accessi

## 2. Frontend

- [x] 2.1 Sezione "Documenti" nella scheda con il download e l'avviso dei dati mancanti; voce della cronologia
- [x] 2.2 Test dei componenti con axe

## 3. Consegna

- [ ] 3.1 PR con CI verde (stati del backlog e collaudo aggiornati dopo il merge)
