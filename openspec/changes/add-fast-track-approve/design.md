## Decisions

### D1 — Corsia veloce consumata all'approvazione
Il brief non dice che cosa succede al flag all'approvazione; al rifiuto (US-404) lo spegne. Lo spegniamo anche
all'approvazione:
- la priorità serve alla coda, e una proposta non è più in coda;
- "corsia veloce attiva" resta sinonimo di "candidatura in valutazione", su cui si basano il blocco dell'annuncio
  (`uq_ticket_fast_track_board_post`) e le indicazioni in bacheca.

Il tipo SPECIAL e il collegamento all'annuncio restano come traccia dell'origine.

### D2 — Stessa logica dell'abbinamento
Approvare vuol dire abbinare la mansione dell'annuncio. La logica è la stessa dell'abbinamento normale (US-602):
- controlli sulla mansione e sull'azienda esclusa;
- blocco della mansione con il flush che fa emergere il conflitto;
- audit della mansione, archiviazione dell'annuncio (D3 dello Sprint 6), storico dell'azienda e notifica al Tutor.

Cambia solo il motivo dell'archiviazione.

### D3 — Prima la corsia veloce
Con la corsia veloce in attesa l'abbinamento a un'altra mansione è rifiutato: lascerebbe l'annuncio bloccato da una
segnalazione che non lo usa. L'operatore approva (US-403) oppure rifiuta (US-404, che la riporta a una lavorazione
normale).

### D4 — L'azienda solo al Call Center
I dati della segnalazione arrivano anche al Tutor, che dell'annuncio non deve vedere l'azienda. La sezione legge
mansione e azienda dalla gestione della bacheca (`GET /board-posts/{id}`, solo Call Center e ADMIN).
