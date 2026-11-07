package it.nexus.services.mail;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

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

    static final String CODE_SUBJECT = EmailSignature.SUBJECT_PREFIX + "Codice per confermare il nuovo indirizzo email";
    static final String NOTICE_SUBJECT = EmailSignature.SUBJECT_PREFIX + "Il tuo indirizzo email è stato cambiato";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(TimeConfig.ZONE);
    private static final String FOOTER = """

            Messaggio automatico di NEXUS, non rispondere.
            """ + EmailSignature.BLOCK;

    private final JavaMailSender mailSender;
    private final ApplicationProperties.Mail properties;
    private final boolean enabled;

    public EmailChangeMailer(JavaMailSender mailSender, ApplicationProperties applicationProperties) {
        this.mailSender = mailSender;
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
        String body = """
                Ciao,

                per confermare questo indirizzo come nuova email del tuo account NEXUS scrivi il codice:

                    %s

                Il codice vale fino alle %s (30 minuti). Se non hai chiesto tu il cambio, ignora questa email: \
                l'indirizzo del tuo account resta quello di prima.
                """.formatted(code, TIME.format(expiresAt)) + FOOTER;
        return send(newEmail, CODE_SUBJECT, body);
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
        String body = """
                Ciao,

                l'indirizzo email del tuo account NEXUS è stato cambiato: da ora le notifiche arrivano al nuovo \
                indirizzo e questo non riceverà più messaggi da NEXUS.

                Se non sei stato tu, contatta subito l'amministratore di NEXUS.
                """ + FOOTER;
        send(event.previousEmail(), NOTICE_SUBJECT, body);
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
