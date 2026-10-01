package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.QueueItemDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;

public interface TicketService {

    /** Segnalazione normale del Tutor corrente: nasce in {@code NUOVA} ed entra nella coda del Call Center. */
    TicketDTO submit(TicketCreateDTO dto);

    /** Coda del Call Center, con filtri facoltativi (id esterni; null = nessun filtro). */
    List<QueueItemDTO> queue(String projectId, String zoneId);

    /** Presa in carico da parte dell'operatore corrente (US-402); 409 se un collega l'ha già presa. */
    QueueItemDTO takeCharge(String ticketId, long expectedVersion);

    /** Segnalazioni aperte assegnate all'operatore corrente. */
    List<QueueItemDTO> assignedToMe();
}
