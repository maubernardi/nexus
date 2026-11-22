package it.nexus.services.mail;

import java.util.Map;

import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.services.messages.MessageTexts;

/**
 * Oggetto e testo dell'email di una notifica (US-1202), con i testi modificabili dall'ADMIN (US-1204): solo il numero
 * della segnalazione, nessun dato personale.
 */
public record NotificationEmail(String subject, String body) {

    /**
     * Email della notifica, con il collegamento alla segnalazione ({@code appUrl} senza barra finale). Oggetto e testo
     * vengono dai testi dei messaggi (US-1204): l'oggetto è «PORTALE NEXUS - Segnalazione n. N - » più la parte
     * distintiva del tipo; collegamento, avvertenza e firma restano fissi.
     */
    public static NotificationEmail of(MessageTexts texts, NotificationType type, String ticketId, Long ticketNumber,
            String appUrl) {
        MessageKey key = MessageKey.of(type);
        Map<String, Object> values = Map.of("numero", ticketNumber);
        String text = texts.render(key, MessagePart.EMAIL_BODY, values);
        String body = """
                %s

                Apri la segnalazione: %s/segnalazioni/%s

                Messaggio automatico di NEXUS, non rispondere. Trovi tutte le notifiche anche nell'app, alla voce \
                Notifiche.
                """.formatted(text, appUrl, ticketId) + EmailSignature.BLOCK;
        String subject = EmailSignature.SUBJECT_PREFIX + "Segnalazione n. %d - %s".formatted(ticketNumber,
                texts.render(key, MessagePart.EMAIL_SUBJECT, values));
        return new NotificationEmail(subject, body);
    }
}
