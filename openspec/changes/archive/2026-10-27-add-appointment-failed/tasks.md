## 1. Backend

- [x] 1.1 `AppointmentFailureCause`; transizione `APPOINTMENT_FAILED`; `MATCH` anche da `RIAPERTO`
- [x] 1.2 `POST /tickets/{id}/appointment-failed`: mansione libera, nessuna esclusione, storico, notifica
- [x] 1.3 "In carico a me": riaperte in cima
- [x] 1.4 IT: flusso completo fino alla nuova proposta, nota obbligatoria con "Altro", 409 e 403

## 2. Frontend

- [x] 2.1 "L'appuntamento non è andato a buon fine": causa e nota, esito con mutateAsync
- [x] 2.2 Riaperte: stato evidenziato in tabella, abbinamento disponibile nel dettaglio
- [x] 2.3 Test con axe; verifica in browser reale (Tutor a 320 px scuro, operatore a 1280 px)

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog aggiornati dopo il merge)
