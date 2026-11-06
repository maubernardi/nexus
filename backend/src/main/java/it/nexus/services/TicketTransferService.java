package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.TicketTransferDTO;
import it.nexus.domain.dto.TicketTransferResultDTO;
import it.nexus.domain.dto.TransferGroupDTO;

/** Spostamento delle segnalazioni aperte da un Tutor a un altro dello stesso progetto (US-111, US-112). */
public interface TicketTransferService {

    /**
     * Segnalazioni aperte del Tutor per progetto (in ordine di nome), con i Tutor che possono riceverle.
     *
     * @throws it.nexus.web.errors.NotFoundException utente inesistente
     */
    List<TransferGroupDTO> openTickets(String tutorId);

    /**
     * Progetti coordinati dall'utente corrente (US-112), per nome: tutte le segnalazioni aperte del progetto e i Tutor
     * attivi che possono riceverle, Coordinatore compreso. Vuoto se non coordina nessun progetto.
     */
    List<TransferGroupDTO> coordination();

    /**
     * Sposta le segnalazioni, tutte aperte e dello stesso progetto, sul Tutor indicato: con ognuna passa anche il
     * beneficiario. Tutto o niente. L'ADMIN sposta in ogni progetto, il Coordinatore solo in quelli che coordina.
     *
     * @throws it.nexus.web.errors.FieldValidationException Tutor non valido o non assegnato al progetto
     * @throws it.nexus.web.errors.BadRequestException segnalazione chiusa o di progetti diversi
     * @throws org.springframework.security.access.AccessDeniedException Tutor che non coordina il progetto
     */
    TicketTransferResultDTO transfer(TicketTransferDTO dto);
}
