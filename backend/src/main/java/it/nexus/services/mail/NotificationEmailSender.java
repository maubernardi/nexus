package it.nexus.services.mail;

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
import lombok.extern.slf4j.Slf4j;

/**
 * Email delle notifiche (US-1202): dopo il commit, in un thread a parte, ogni notifica in app arriva anche via email
 * alla stessa persona. Un invio fallito non annulla nulla: resta la notifica in app e un avviso nel log, senza
 * l'indirizzo. Spente se {@code nexus.mail.enabled} è falso o manca la password SMTP.
 */
@Slf4j
@Component
public class NotificationEmailSender {

    private final JavaMailSender mailSender;
    private final ApplicationProperties.Mail properties;
    private final boolean enabled;

    public NotificationEmailSender(JavaMailSender mailSender, ApplicationProperties applicationProperties) {
        this.mailSender = mailSender;
        this.properties = applicationProperties.getMail();
        boolean passwordMissing = mailSender instanceof JavaMailSenderImpl impl && !StringUtils.hasText(impl.getPassword());
        this.enabled = properties.isEnabled() && !passwordMissing;
        if (properties.isEnabled() && passwordMissing) {
            log.warn("Email delle notifiche spente: manca la password SMTP (NEXUS_SMTP_PASSWORD nel .env del server)");
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        send(event);
    }

    /** Invia l'email della notifica, se le email sono accese e la persona ha un indirizzo. */
    void send(NotificationCreatedEvent event) {
        if (!enabled || !StringUtils.hasText(event.recipientEmail()) || event.ticketId() == null) {
            return;
        }
        NotificationEmail email = NotificationEmail.of(event.type(), event.ticketId(), event.ticketNumber(),
                properties.getAppUrl());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFrom());
        message.setTo(event.recipientEmail());
        message.setSubject(email.subject());
        message.setText(email.body());
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Email della notifica {} ({}) non inviata: {}", event.notificationId(), event.type(),
                    e.getMessage());
        }
    }
}
