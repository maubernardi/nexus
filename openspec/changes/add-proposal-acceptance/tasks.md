## 1. Backend

- [x] 1.1 Transizione `ACCEPT` e `POST /tickets/{id}/accept` (solo il Tutor della segnalazione)
- [x] 1.2 Timer: inizio e scadenza (fine del 7° giorno lavorativo, ora di Roma); `Clock` con il fuso di Roma
- [x] 1.3 Audit `ACCEPT` con dettaglio in cronologia; `PROPOSTA_ACCOLTA` nello storico dell'azienda
- [x] 1.4 `myTicket` e `timerDeadline` nel dettaglio
- [x] 1.5 IT: accettazione completa (stato, timer, mansione, storico, cronologia), ruoli e visibilità, 409

## 2. Frontend

- [x] 2.1 "Accetta la proposta" con conferma ed esito; scadenza del timer nella sezione della proposta
- [x] 2.2 Test con axe; verifica in browser reale (Tutor, collega, Call Center; 320/1280 px)

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog aggiornati dopo il merge)
