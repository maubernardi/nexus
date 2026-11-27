package it.nexus.domain.enumeration;

import java.util.List;

/**
 * Messaggi di NEXUS con i testi modificabili dall'ADMIN (US-1204): una voce per ogni tipo di notifica, con il testo
 * nell'app, l'oggetto e il testo dell'email, più le due email del cambio di indirizzo (US-110), che hanno solo l'email.
 * I testi predefiniti sono in {@code MessageDefaults}.
 */
public enum MessageKey {
    CATEGORY_CHANGED, CATEGORY_APPROVED, PROPOSAL_SENT, PROPOSAL_ACCEPTED, APPOINTMENT_SET, APPOINTMENT_FAILED,
    PROPOSAL_REJECTED, TIMER_REMINDER, TIMER_EXPIRED, BOARD_RELAUNCH_REQUESTED, FAST_TRACK_REJECTED,
    FAST_TRACK_DROPPED, INTERNSHIP_STARTED, CLOSURE_REPORT_SUBMITTED, TICKET_TRANSFERRED, INTERNSHIP_ENDING,
    INTERNSHIP_OVERDUE, TRAINING_SHIP_PROPOSED, CONTACT_REQUEST_CLOSED,
    /** Codice per confermare il nuovo indirizzo email (US-110). */
    EMAIL_CHANGE_CODE,
    /** Avviso al vecchio indirizzo dopo il cambio (US-110). */
    EMAIL_CHANGE_NOTICE;

    /** Il messaggio della notifica: stesso nome del tipo. */
    public static MessageKey of(NotificationType type) {
        return valueOf(type.name());
    }

    public boolean isNotification() {
        return this != EMAIL_CHANGE_CODE && this != EMAIL_CHANGE_NOTICE;
    }

    /** Le parti del messaggio: le notifiche hanno anche il testo nell'app. */
    public List<MessagePart> parts() {
        return isNotification() ? List.of(MessagePart.APP, MessagePart.EMAIL_SUBJECT, MessagePart.EMAIL_BODY)
                : List.of(MessagePart.EMAIL_SUBJECT, MessagePart.EMAIL_BODY);
    }

    /** Segnaposto ammessi nei testi, da scrivere tra graffe: {@code {numero}}. */
    public List<String> placeholders() {
        return switch (this) {
            case EMAIL_CHANGE_CODE -> List.of("codice", "ora");
            case EMAIL_CHANGE_NOTICE -> List.of();
            // il numero è della richiesta di contatto, non di una segnalazione (US-512)
            case CONTACT_REQUEST_CLOSED -> List.of("numero", "esito", "nota");
            default -> List.of("numero");
        };
    }
}
