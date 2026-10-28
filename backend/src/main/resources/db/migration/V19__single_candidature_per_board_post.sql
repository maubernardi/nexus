-- Una sola candidatura per annuncio (US-306, review dello Sprint 6 §8.1): la prima segnalazione speciale blocca
-- l'annuncio finché il Call Center non decide. Il rifiuto della corsia veloce (US-404) toglie il fast-track e libera
-- l'annuncio; l'approvazione (US-403) lo archivia. Il vincolo nel database protegge anche dalle richieste concorrenti.
CREATE UNIQUE INDEX uq_ticket_fast_track_board_post ON ticket (board_post_id) WHERE is_fast_track;
