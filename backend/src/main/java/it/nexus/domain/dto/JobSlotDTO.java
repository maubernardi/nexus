package it.nexus.domain.dto;

import it.nexus.domain.enumeration.JobSlotStatus;

/** Mansione di un'azienda; {@code blockedByTicketNumber} indica la segnalazione che la tiene bloccata. */
public record JobSlotDTO(
        String id,
        String companyId,
        String title,
        String description,
        ReferenceItemDTO jobCategory,
        ReferenceItemDTO zone,
        JobSlotStatus status,
        Long blockedByTicketNumber,
        boolean active,
        long version) {
}
