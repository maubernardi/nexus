## Decisions

### D1 — Transizioni senza cambio di stato
Rifiuto e decadenza lasciano la segnalazione in lavorazione: sono transizioni da `IN_LAVORAZIONE` a `IN_LAVORAZIONE`,
così passano dalla macchina a stati (ruoli, versione, storico, audit con il motivo) come tutte le altre scelte. La
cronologia mostra l'azione e il motivo; lo storico degli stati non mostra il cambio vuoto (corretto con US-403).

### D2 — Un servizio per la fine della corsia veloce
La decadenza nasce in `BoardPostService` (archiviazione), il rifiuto in `TicketService`. `TicketService` dipende già
dalla bacheca, quindi la logica comune sta in `FastTrackService`, che dipende solo da macchina a stati, segnalazioni e
notifiche.

### D3 — Due azioni, due notifiche
Il Tutor deve capire che cosa è successo: un rifiuto deciso dal Call Center (`FAST_TRACK_REJECT`,
`FAST_TRACK_REJECTED`) è diverso da un annuncio che non c'è più (`FAST_TRACK_DROP`, `FAST_TRACK_DROPPED`). In entrambi
i casi il motivo è nella cronologia.

### D4 — Autore della decadenza
La decadenza la provoca l'operatore che archivia l'annuncio o rende indisponibile la mansione: è lui l'autore
nell'audit, con il motivo che spiega la causa ("Annuncio #N archiviato: Mansione abbinata alla segnalazione n. M").
