package it.nexus.services.mail;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import it.nexus.config.ApplicationProperties;
import it.nexus.config.TimeConfig;
import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;
import it.nexus.services.MessageTextService;
import it.nexus.services.messages.MessageTexts;
import lombok.extern.slf4j.Slf4j;

/**
 * Email del cambio di indirizzo (US-110).
 * <ul>
 * <li>codice di conferma al nuovo indirizzo: invio sincrono, se non parte la richiesta non si salva;</li>
 * <li>avviso al vecchio indirizzo dopo il commit, asincrono: un errore non annulla il cambio.</li>
 * </ul>
 * Con le email spente il codice si scrive nel log solo se {@code nexus.mail.log-otp} (sviluppo locale).
 */
@Slf4j
@Component
public class EmailChangeMailer {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(TimeConfig.ZONE);
    private static final String FOOTER = """

            Messaggio automatico di NEXUS, non rispondere.
            """ + EmailSignature.BLOCK;

    private final JavaMailSender mailSender;
    private final ApplicationProperties.Mail properties;
    private final MessageTextService messageTextService;
    private final boolean enabled;

    public EmailChangeMailer(JavaMailSender mailSender, ApplicationProperties applicationProperties,
            MessageTextService messageTextService) {
        this.mailSender = mailSender;
        this.messageTextService = messageTextService;
        this.properties = applicationProperties.getMail();
        boolean passwordMissing = mailSender instanceof JavaMailSenderImpl impl && !StringUtils.hasText(impl.getPassword());
        this.enabled = properties.isEnabled() && !passwordMissing;
    }

    /**
     * Invia il codice al nuovo indirizzo.
     *
     * @return falso se l'email non è partita (email spente o errore SMTP): la richiesta non va salvata
     */
    public boolean sendCode(String newEmail, String code, Instant expiresAt) {
        if (!enabled) {
            if (properties.isLogOtp()) {
                log.warn("Email spente (sviluppo locale): codice per il cambio di indirizzo {}", code);
                return true;
            }
            log.warn("Codice per il cambio di indirizzo non inviato: email spente");
            return false;
        }
        // testi modificabili dall'ADMIN (US-1204); avvertenza e firma restano fisse
        MessageTexts texts = messageTextService.current();
        Map<String, Object> values = Map.of("codice", code, "ora", TIME.format(expiresAt));
        String body = texts.render(MessageKey.EMAIL_CHANGE_CODE, MessagePart.EMAIL_BODY, values) + "\n" + FOOTER;
        return send(newEmail, subject(texts, MessageKey.EMAIL_CHANGE_CODE, values), body);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailChanged(EmailChangedEvent event) {
        sendNotice(event);
    }

    /** Avviso al vecchio indirizzo; un errore resta nel log. */
    void sendNotice(EmailChangedEvent event) {
        if (!enabled || !StringUtils.hasText(event.previousEmail())) {
            return;
        }
        MessageTexts texts = messageTextService.current();
        String body = texts.render(MessageKey.EMAIL_CHANGE_NOTICE, MessagePart.EMAIL_BODY, Map.of()) + "\n" + FOOTER;
        send(event.previousEmail(), subject(texts, MessageKey.EMAIL_CHANGE_NOTICE, Map.of()), body);
    }

    /** Oggetto: «PORTALE NEXUS - » e il testo dell'ADMIN, o il predefinito. */
    static String subject(MessageTexts texts, MessageKey key, Map<String, ?> values) {
        return EmailSignature.SUBJECT_PREFIX + texts.render(key, MessagePart.EMAIL_SUBJECT, values);
    }

    private boolean send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
            return true;
        } catch (MailException e) {
            log.warn("Email «{}» non inviata: {}", subject, e.getMessage());
            return false;
        }
    }
}
