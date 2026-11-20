package it.nexus.domain.dto;

/** Segnalazione a cui è assegnata una mansione (US-605), con il tipo di tirocinio (assente se non indicato). */
public record JobSlotAssignmentDTO(long ticketNumber, ReferenceItemDTO internshipType) {
}
