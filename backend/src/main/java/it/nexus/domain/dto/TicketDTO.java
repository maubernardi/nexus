package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;

/** Ticket in sintesi; {@code version} va rimandata nelle richieste di cambio di stato. */
public record TicketDTO(
        String id,
        Long number,
        TicketType type,
        TicketStatus status,
        boolean fastTrack,
        long version,
        BeneficiarySummaryDTO beneficiary,
        ReferenceItemDTO project,
        ReferenceItemDTO requestedJobCategory,
        Instant createdAt) {
}
