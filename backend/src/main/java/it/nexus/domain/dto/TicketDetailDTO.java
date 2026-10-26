package it.nexus.domain.dto;

import java.time.Instant;
import java.time.LocalDate;

import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;

/** Dettaglio della segnalazione (US-305); {@code proposal} è la mansione abbinata, se c'è. */
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
        Instant createdAt,
        /** L'utente corrente è il Tutor della segnalazione: può accettare la proposta (US-701). */
        boolean myTicket,
        /** Ultimo giorno lavorativo del timer della proposta accettata (US-701), se il timer è attivo. */
        LocalDate timerDeadline) {
}
