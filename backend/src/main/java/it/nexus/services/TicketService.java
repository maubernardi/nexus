package it.nexus.services;

import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;

public interface TicketService {

    /** Segnalazione normale del Tutor corrente: nasce in {@code NUOVA} ed entra nella coda del Call Center. */
    TicketDTO submit(TicketCreateDTO dto);
}
