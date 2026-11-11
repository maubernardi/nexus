package it.nexus.domain.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Report di restituzione inviato (US-1101), in sola lettura nel dettaglio della segnalazione. Formula e motivo restano
 * anche se l'ADMIN disattiva la voce; {@code submittedBy} è il nome di chi l'ha inviato. {@code actualEndDate} (US-1009)
 * manca nei report inviati prima.
 */
public record ClosureReportDTO(
        LocalDate actualEndDate,
        int companySatisfaction,
        int beneficiarySatisfaction,
        String objectives,
        String difficulties,
        boolean hired,
        ReferenceItemDTO hiringFormula,
        ReferenceItemDTO noHireReason,
        boolean jobSlotStillAvailable,
        Instant submittedAt,
        String submittedBy) {
}
