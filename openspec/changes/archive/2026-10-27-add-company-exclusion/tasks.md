## 1. Backend

- [x] 1.1 `GET`/`POST /tickets/{id}/exclusions` (Call Center e ADMIN), motivo obbligatorio, stati ammessi, doppione 409
- [x] 1.2 Audit `COMPANY_EXCLUDE` con il motivo (cronologia); esclusione nello storico dell'azienda
- [x] 1.3 IT: esclusione e compatibili, elenco, cronologia, storico, motivo obbligatorio, 409 e 403

## 2. Frontend

- [x] 2.1 "Escludi questa azienda" nella finestra "Dettagli"; esito con il focus
- [x] 2.2 Elenco "Aziende escluse" sotto le compatibili
- [x] 2.3 Test con axe; verifica in browser reale (320/1280 px, chiaro/scuro)

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog aggiornati dopo il merge)
