package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.BoardPostStatus;

/**
 * Annuncio della bacheca per Call Center e ADMIN (US-902), con l'azienda: mai per il Tutor. {@code project == null}
 * significa visibile a tutti; {@code createdFromTicket*} indica la segnalazione del rilancio.
 */
public record BoardPostDTO(
        String id,
        Long number,
        String title,
        BoardPostStatus status,
        JobSlotMatchDTO jobSlot,
        ReferenceItemDTO project,
        Integer ageMin,
        Integer ageMax,
        Integer weeklyHours,
        Integer durationMonths,
        String notes,
        Instant publishedAt,
        Instant madePublicAt,
        String createdFromTicketId,
        Long createdFromTicketNumber,
        Instant createdAt,
        long version) {
}
