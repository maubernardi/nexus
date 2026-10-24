package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.TicketStatus;

/**
 * Voce della cronologia di una segnalazione (US-305): quando, chi, quale azione, eventuale cambio di stato, dettaglio
 * leggibile (es. "«Magazziniere» presso Alfa Logistica") e nota.
 */
public record TicketEventDTO(
        Instant at,
        String authorName,
        String action,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        String detail,
        String note) {
}
