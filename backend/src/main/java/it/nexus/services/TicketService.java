package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.JobSlotMatchDTO;
import it.nexus.domain.dto.MatchRequestDTO;
import it.nexus.domain.dto.QueueItemDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.dto.TicketDetailDTO;

public interface TicketService {

    /** Segnalazione normale del Tutor corrente: nasce in {@code NUOVA} ed entra nella coda del Call Center. */
    TicketDTO submit(TicketCreateDTO dto);

    /** Coda del Call Center, con filtri facoltativi (id esterni; null = nessun filtro). */
    List<QueueItemDTO> queue(String projectId, String zoneId);

    /** Presa in carico da parte dell'operatore corrente (US-402); 409 se un collega l'ha già presa. */
    QueueItemDTO takeCharge(String ticketId, long expectedVersion);

    /** Segnalazioni aperte assegnate all'operatore corrente. */
    List<QueueItemDTO> assignedToMe();

    /** Dettaglio per la lavorazione del Call Center (US-601/602). */
    TicketDetailDTO getForWork(String ticketId);

    /** Mansioni proponibili per la segnalazione; filtri facoltativi (id esterni, null = tutte). */
    List<JobSlotMatchDTO> compatibleJobSlots(String ticketId, String zoneId, String jobCategoryId);

    /**
     * Abbina la mansione e propone il beneficiario all'azienda: ticket in PROPOSTA_AZIENDA, mansione BLOCCATA.
     *
     * @throws it.nexus.web.errors.ConflictException se ticket o mansione sono cambiati o la mansione non è più proponibile
     */
    TicketDetailDTO match(String ticketId, MatchRequestDTO request);
}
