package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.ContactRequestStatus;

/**
 * Richiesta di contatto (US-507) con l'azienda come l'ha indicata il Tutor. {@code companyId} c'è quando l'operatore
 * l'ha collegata all'anagrafica, alla chiusura (US-512). Gli assenti sono omessi dall'API.
 */
public record ContactRequestDTO(
        String id,
        Long number,
        Instant createdAt,
        /** Nome e cognome del Tutor che l'ha inviata. */
        String requestedByName,
        String companyId,
        String companyName,
        ReferenceItemDTO zone,
        ReferenceItemDTO businessCategory,
        ContactRequestStatus status,
        /** Nome e cognome dell'operatore che l'ha in carico. */
        String assignedOperatorName,
        Instant takenAt,
        long version) {
}
