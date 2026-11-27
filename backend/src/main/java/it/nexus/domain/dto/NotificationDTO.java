package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.NotificationType;

/**
 * Notifica in app con il testo già composto. {@code companyId} c'è negli avvisi che portano alla scheda dell'azienda invece
 * che alla segnalazione (US-515).
 */
public record NotificationDTO(String id, NotificationType type, String ticketId, Long ticketNumber, String companyId,
        /** Richiesta di contatto a cui porta l'avviso (US-512). */
        String contactRequestId, Long contactRequestNumber, Instant createdAt, boolean read, String text) {
}
