## 1. Backend

- [x] 1.1 `spring-boot-starter-mail`, configurazione SMTP IONOS, `nexus.mail` (acceso, mittente, indirizzo dell'app)
- [x] 1.2 Evento alla creazione della notifica; invio dopo il commit, asincrono, che non blocca mai
- [x] 1.3 Testi delle email per ogni tipo di notifica, senza dati personali
- [x] 1.4 Test: testi, invio acceso/spento/senza password/errore SMTP, evento pubblicato

## 2. Deploy

- [x] 2.1 Compose: email accese, password da `NEXUS_SMTP_PASSWORD`, indirizzo dell'app; README per il PO

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog e collaudo aggiornati dopo il merge)
- [ ] 3.2 Il PO aggiunge la password al `.env`; prova di invio reale dopo il deploy
