package it.nexus.services;

import it.nexus.domain.BoardPost;
import it.nexus.domain.Ticket;

/**
 * Fine della corsia veloce senza approvazione: rifiuto del Call Center (US-404) o annuncio archiviato mentre la
 * candidatura aspetta (review dello Sprint 6, §8.1). La segnalazione resta SPECIAL e in lavorazione, il Tutor è avvisato.
 * Servizio a sé: lo usano sia le segnalazioni sia la bacheca, senza dipendenze circolari.
 */
public interface FastTrackService {

    /** Rifiuto del Call Center, con il motivo; l'annuncio resta pubblicato e torna candidabile. */
    Ticket reject(Ticket ticket, long expectedVersion, String reason);

    /** L'annuncio è stato archiviato: se una candidatura aspettava, perde la corsia veloce con il motivo. */
    void dropPending(BoardPost post, String reason);
}
