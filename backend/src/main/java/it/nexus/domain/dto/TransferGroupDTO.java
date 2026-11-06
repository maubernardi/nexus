package it.nexus.domain.dto;

import java.util.List;

/**
 * Segnalazioni aperte di un Tutor in un progetto, con i Tutor attivi dello stesso progetto che possono riceverle
 * (US-111).
 */
public record TransferGroupDTO(ReferenceItemDTO project, List<TicketListItemDTO> tickets, List<TutorOptionDTO> targets) {
}
