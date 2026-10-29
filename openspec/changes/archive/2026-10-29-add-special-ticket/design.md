## Decisions

### D1 — Il blocco è la corsia veloce
Un annuncio è bloccato finché esiste una segnalazione con la corsia veloce collegata a esso. Non serve un nuovo
stato dell'annuncio:
- il rifiuto (US-404) toglie la corsia veloce e l'annuncio torna candidabile;
- l'approvazione (US-403) archivia l'annuncio.

Il vincolo sta nel database (`uq_ticket_fast_track_board_post`, indice parziale `WHERE is_fast_track`), così regge anche
due candidature nello stesso istante. Il servizio controlla prima, per rispondere con il motivo.

### D2 — Visibilità
Per il Tutor una bozza o un annuncio riservato a un altro progetto non esistono (`404`, come per le segnalazioni
altrui). Un annuncio archiviato mentre il Tutor compilava risponde `409` con il motivo.

### D3 — Progetto della segnalazione
Per un annuncio riservato vale il suo progetto: il Tutor lo vede solo se ne fa parte. Per un annuncio aperto a tutti
il Tutor sceglie tra i suoi progetti, e con un solo progetto la scelta è già fatta.

### D4 — Una pagina per la candidatura
La candidatura ha una pagina sua (`/bacheca/:id/candidatura`), pensata per lo smartphone: ripete la scheda
dell'annuncio, perché il Tutor sappia a cosa sta candidando, e il modulo. La scheda dell'annuncio diventa un componente
comune (`TutorBoardPostCard`).

### D5 — Annuncio archiviato con una candidatura in attesa
Il caso è deciso nella review (interpretazione del team: la segnalazione perde la corsia veloce, con il motivo e la
notifica al Tutor). Si realizza con US-404, che introduce la perdita della corsia veloce.
