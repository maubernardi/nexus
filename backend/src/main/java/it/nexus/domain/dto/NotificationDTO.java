package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.NotificationType;

/** Notifica in app: il testo lo compone l'interfaccia dal tipo e dal numero della segnalazione. */
public record NotificationDTO(String id, NotificationType type, String ticketId, Long ticketNumber, Instant createdAt,
        boolean read) {
}
