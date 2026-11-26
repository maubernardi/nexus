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
        /** Motivo di chiusura (US-1103); assente nei report inviati prima. */
        ReferenceItemDTO closureReason,
        /** Descrizione dell'interruzione anticipata; assente con la chiusura regolare. */
        String interruptionDetails,
        int companySatisfaction,
        int beneficiarySatisfaction,
        String objectives,
        String difficulties,
        boolean hired,
        ReferenceItemDTO hiringFormula,
        ReferenceItemDTO noHireReason,
        boolean jobSlotStillAvailable,
        Instant submittedAt,
        String submittedBy,
        /** Valutazione dell'azienda (US-1104): voti da 1 a 5 e nota; assenti nei report inviati prima. */
        Integer companyInteraction,
        Integer companyWelcome,
        Integer companyTraining,
        String companyReviewNote,
        /** Il Tutor ha proposto l'azienda come nave scuola (US-515). */
        boolean trainingShipProposed) {
}
