## 1. Backend

- [x] 1.1 Transizioni `FAST_TRACK_REJECT` e `FAST_TRACK_DROP`; notifiche `FAST_TRACK_REJECTED` e `FAST_TRACK_DROPPED`
- [x] 1.2 `FastTrackService`; `POST /tickets/{id}/fast-track/reject` con motivo obbligatorio
- [x] 1.3 Decadenza all'archiviazione dell'annuncio, a mano o automatica
- [x] 1.4 IT (`FastTrackRejectIT`): rifiuto e annuncio di nuovo candidabile, lavorazione normale dopo, motivo, presa in carico, ruolo, una volta sola, decadenza a mano e per abbinamento

## 2. Frontend

- [x] 2.1 "Rifiuta la corsia veloce" con motivo; avviso nella conferma di archiviazione; notifiche e cronologia
- [x] 2.2 Test dei componenti con axe

## 3. Consegna

- [x] 3.1 PR con CI verde (stati del backlog e collaudo aggiornati dopo il merge)
