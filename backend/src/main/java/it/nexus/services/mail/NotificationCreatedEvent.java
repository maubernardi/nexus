package it.nexus.services.mail;

import java.util.Map;

import it.nexus.domain.enumeration.NotificationType;

/**
 * Notifica in app appena salvata (US-1201): dopo il commit parte anche l'email (US-1202). Porta solo ciò che serve
 * all'email; l'indirizzo è un dato personale, mai nei log né in {@link #toString()}.
 */
public record NotificationCreatedEvent(Long notificationId, String recipientEmail, NotificationType type,
        String ticketId, Long ticketNumber, String companyId, ContactRequestTarget contactRequest) {

    /** Richiesta di contatto dell'avviso (US-512): id, numero e i valori dei segnaposto ({@code numero}, {@code esito}, {@code nota}). */
    public record ContactRequestTarget(String id, Long number, Map<String, Object> values) {
    }

    @Override
    public String toString() {
        return "NotificationCreatedEvent[notificationId=" + notificationId + ", type=" + type + "]";
    }
}
