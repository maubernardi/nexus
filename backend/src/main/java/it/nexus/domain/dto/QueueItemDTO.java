package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;

/** Voce della coda del Call Center; {@code version} serve alla presa in carico (US-402). */
public record QueueItemDTO(
        String id,
        Long number,
        TicketType type,
        TicketStatus status,
        boolean fastTrack,
        long version,
        CandidateSummaryDTO candidate,
        ReferenceItemDTO project,
        ReferenceItemDTO requestedJobCategory,
        String tutorName,
        Instant createdAt) {
}
