package it.nexus.services.mail;

import it.nexus.domain.enumeration.NotificationType;

/**
 * Notifica in app appena salvata (US-1201): dopo il commit parte anche l'email (US-1202). Porta solo ciò che serve
 * all'email; l'indirizzo è un dato personale, mai nei log né in {@link #toString()}.
 */
public record NotificationCreatedEvent(Long notificationId, String recipientEmail, NotificationType type,
        String ticketId, Long ticketNumber) {

    @Override
    public String toString() {
        return "NotificationCreatedEvent[notificationId=" + notificationId + ", type=" + type + "]";
    }
}
