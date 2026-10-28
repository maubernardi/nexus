## 1. Backend

- [x] 1.1 Migrazione V19 `uq_ticket_fast_track_board_post` e test del vincolo per nome
- [x] 1.2 Transizione `SUBMIT_SPECIAL`, audit con `fastTrack` e `boardPostId`
- [x] 1.3 `POST /tickets/special`: visibilità, beneficiario, una sola candidatura, progetto, tipologia
- [x] 1.4 `GET /board/{id}`, `candidatureInProgress`, candidatura nella gestione, annuncio nel dettaglio, cronologia
- [x] 1.5 `WorkflowScenarios` (A7) e IT: candidatura, coda, riservato, doppia candidatura, beneficiario, visibilità, ruolo

## 2. Frontend

- [x] 2.1 "Candida un beneficiario" / "Candidatura in valutazione" in bacheca; scheda dell'annuncio come componente
- [x] 2.2 Pagina della candidatura con esito; annuncio nel dettaglio della segnalazione; riga nella gestione
- [x] 2.3 Test dei componenti con axe

## 3. Consegna

- [ ] 3.1 PR con CI verde (stati del backlog e collaudo aggiornati dopo il merge)
