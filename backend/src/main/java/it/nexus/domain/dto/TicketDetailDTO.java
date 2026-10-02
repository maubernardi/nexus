package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;

/** Segnalazione vista dal Call Center durante la lavorazione; {@code proposal} è la mansione abbinata, se c'è. */
public record TicketDetailDTO(
        String id,
        Long number,
        TicketType type,
        TicketStatus status,
        boolean fastTrack,
        long version,
        BeneficiarySummaryDTO beneficiary,
        /** Zona di residenza del beneficiario: filtro predefinito della ricerca delle compatibili. */
        ReferenceItemDTO beneficiaryZone,
        ReferenceItemDTO project,
        ReferenceItemDTO requestedJobCategory,
        String requestedJobFreeText,
        String tutorName,
        String assignedOperatorName,
        boolean assignedToMe,
        JobSlotMatchDTO proposal,
        Instant createdAt) {
}
