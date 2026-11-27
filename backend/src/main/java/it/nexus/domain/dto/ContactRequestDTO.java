package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.ContactRequestStatus;

/**
 * Richiesta di contatto (US-507). Per un'azienda già presente c'è {@code companyId}, con nome, zona e categoria
 * dell'azienda; per una nuova solo quelli scritti dal Tutor. Gli assenti sono omessi dall'API.
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
