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
     * Email della notifica, con il collegamento alla segnalazione ({@code appUrl} senza barra finale) o, con
     * {@code companyId}, alla scheda dell'azienda (US-515). Oggetto e testo vengono dai testi dei messaggi (US-1204):
     * l'oggetto è «PORTALE NEXUS - Segnalazione n. N - » più la parte distintiva del tipo; collegamento, avvertenza e
     * firma restano fissi.
     */
    public static NotificationEmail of(MessageTexts texts, NotificationType type, String ticketId, Long ticketNumber,
            String companyId, String appUrl) {
        MessageKey key = MessageKey.of(type);
        Map<String, Object> values = Map.of("numero", ticketNumber);
        String text = texts.render(key, MessagePart.EMAIL_BODY, values);
        String link = companyId == null ? "Apri la segnalazione: %s/segnalazioni/%s".formatted(appUrl, ticketId)
                : "Apri la scheda dell'azienda: %s/aziende/%s".formatted(appUrl, companyId);
        String body = """
                %s

                %s

                Messaggio automatico di NEXUS, non rispondere. Trovi tutte le notifiche anche nell'app, alla voce \
                Notifiche.
                """.formatted(text, link) + EmailSignature.BLOCK;
        String subject = EmailSignature.SUBJECT_PREFIX + "Segnalazione n. %d - %s".formatted(ticketNumber,
                texts.render(key, MessagePart.EMAIL_SUBJECT, values));
        return new NotificationEmail(subject, body);
    }

    /**
     * Email di un avviso sulla richiesta di contatto (US-512): oggetto «PORTALE NEXUS - Richiesta di contatto n. N - »
     * più la parte distintiva, il testo con i segnaposto ({@code numero}, {@code esito}, {@code nota}) e il collegamento
     * alla richiesta.
     */
    public static NotificationEmail ofContactRequest(MessageTexts texts, NotificationType type, String requestId,
            Long requestNumber, Map<String, ?> values, String appUrl) {
        MessageKey key = MessageKey.of(type);
        String text = texts.render(key, MessagePart.EMAIL_BODY, values);
        String body = """
                %s

                Apri la richiesta: %s/richieste/%s

                Messaggio automatico di NEXUS, non rispondere. Trovi tutte le notifiche anche nell'app, alla voce \
                Notifiche.
                """.formatted(text, appUrl, requestId) + EmailSignature.BLOCK;
        String subject = EmailSignature.SUBJECT_PREFIX + "Richiesta di contatto n. %d - %s".formatted(requestNumber,
                texts.render(key, MessagePart.EMAIL_SUBJECT, values));
        return new NotificationEmail(subject, body);
    }
}
