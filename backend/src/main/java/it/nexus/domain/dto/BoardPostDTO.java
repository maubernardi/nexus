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
        long version,
        /** Da quando l'annuncio riservato si può rendere visibile a tutti (7 giorni dalla pubblicazione, US-903). */
        Instant makePublicFrom,
        /** Segnalazione speciale candidata all'annuncio e in attesa della decisione del Call Center (US-306). */
        String candidatureTicketId,
        Long candidatureTicketNumber) {

    public BoardPostDTO withCandidature(String ticketId, Long ticketNumber) {
        return new BoardPostDTO(id, number, title, status, jobSlot, project, ageMin, ageMax, weeklyHours, durationMonths,
                notes, publishedAt, madePublicAt, createdFromTicketId, createdFromTicketNumber, createdAt, version,
                makePublicFrom, ticketId, ticketNumber);
    }
}
