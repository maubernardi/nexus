## 1. Backend

- [x] 1.1 Transizione `FAST_TRACK_APPROVE`; `POST /tickets/{id}/fast-track/approve`
- [x] 1.2 Logica comune con l'abbinamento; corsia veloce consumata; abbinamento rifiutato con la corsia veloce in attesa
- [x] 1.3 Cronologia: voce dell'approvazione; nessun cambio di stato vuoto dopo la presa in carico di una speciale
- [x] 1.4 IT (`FastTrackApproveIT`, con `WorkflowScenarios`): operazione completa, presa in carico, mansione o annuncio non disponibili, ruolo e versione, abbinamento rifiutato, cronologia

## 2. Frontend

- [x] 2.1 Sezione "Corsia veloce" con mansione e azienda dell'annuncio, approvazione con conferma ed esito
- [x] 2.2 Test dei componenti con axe

## 3. Consegna

- [ ] 3.1 PR con CI verde (stati del backlog e collaudo aggiornati dopo il merge)
