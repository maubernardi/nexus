## 1. Backend

- [x] 1.1 `getDetail` con visibilità per ruolo; `GET /tickets/{id}` al posto di `/work`
- [x] 1.2 `history`: eventi dall'audit con dettagli e nomi; integrazione dello storico per le segnalazioni precedenti all'audit
- [x] 1.3 IT: visibilità (proprio, collega di progetto, altro progetto 404, Call Center), cronologia completa, solo storico, misto storico+audit

## 2. Frontend

- [x] 2.1 Pagina unica `/segnalazioni/:id` con cronologia; contenuti per ruolo; reindirizzamento da `/lavorazioni/:id`; link dalla Home per tutti
- [x] 2.2 Link della tabella con `aria-label` (area cliccabile corretta)
- [x] 2.3 Test con axe; verifica in browser reale (Tutor e Call Center, 320/1280 px, chiaro/scuro, reindirizzamento)

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog aggiornati dopo il merge)
