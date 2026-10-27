## 1. Backend

- [x] 1.1 Transizione `PROPOSAL_REJECT` e `POST /tickets/{id}/reject` (solo il Tutor della segnalazione)
- [x] 1.2 Mansione libera, azienda esclusa con il motivo, storico, notifica all'operatore
- [x] 1.3 IT: flusso completo (esclusione dalle compatibili, riaperta in cima), motivo obbligatorio, 409 e 403

## 2. Frontend

- [x] 2.1 "Rifiuta la proposta" con motivo, esito con mutateAsync; voce di cronologia; testo della notifica
- [x] 2.2 Test con axe; verifica in browser reale (Tutor a 320 px; esclusione verificata sulle compatibili)

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog aggiornati dopo il merge)
