## 1. Backend

- [x] 1.1 V17: rinomina dello stato (dati e vincoli) e `category_change_unseen`; `TicketStatus.fromStored`
- [x] 1.2 Invio con tipologia nuova (`@JobChoice`, nome già nel catalogo rifiutato)
- [x] 1.3 Transizioni e API: approva (tipologia nel catalogo, codice univoco), respingi con motivo, avviso visto
- [x] 1.4 Vista `TO_APPROVE`; dettagli in cronologia; dati demo aggiornati
- [x] 1.5 IT: invio, vista, approvazione, nome già presente, codice univoco, respingimento con avviso, ruoli, 409

## 2. Frontend

- [x] 2.1 Modulo: "La mansione non è nell'elenco"; esito "in attesa di approvazione"
- [x] 2.2 Home: vista "Da approvare", tipologia nuova nella tabella, evidenza "Tipologia cambiata"
- [x] 2.3 Dettaglio: decisione del Call Center, avviso al Tutor segnato come visto
- [x] 2.4 Test con axe; verifica in browser reale del flusso completo (320/1280 px, chiaro/scuro)

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog aggiornati dopo il merge)
