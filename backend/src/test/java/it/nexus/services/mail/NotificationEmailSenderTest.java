package it.nexus.services.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import it.nexus.config.ApplicationProperties;
import it.nexus.domain.enumeration.NotificationType;

/** Invio delle email delle notifiche (US-1202): solo se accese e con la password, mai bloccante. */
class NotificationEmailSenderTest {

    private static final NotificationCreatedEvent EVENT = new NotificationCreatedEvent(1L, "operatore@nexus.local",
            NotificationType.CLOSURE_REPORT_SUBMITTED, "T1", 34L);

    private final JavaMailSenderImpl mailSender = mock(JavaMailSenderImpl.class);

    private NotificationEmailSender sender(boolean enabled, String password) {
        when(mailSender.getPassword()).thenReturn(password);
        ApplicationProperties properties = new ApplicationProperties();
        properties.getMail().setEnabled(enabled);
        properties.getMail().setAppUrl("https://portalenexus.it");
        return new NotificationEmailSender(mailSender, properties);
    }

    @Test
    void accese_inviaAllaStessaPersonaDalMittenteDelDominio() {
        sender(true, "segreta").send(EVENT);

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        assertThat(message.getValue().getFrom()).isEqualTo("NEXUS <noreply@portalenexus.it>");
        assertThat(message.getValue().getTo()).containsExactly("operatore@nexus.local");
        assertThat(message.getValue().getSubject()).isEqualTo("PORTALE NEXUS - Segnalazione n. 34");
        assertThat(message.getValue().getText())
                .startsWith("Il Tutor ha inviato il report di restituzione della segnalazione n. 34")
                .contains("https://portalenexus.it/segnalazioni/T1");
    }

    @Test
    void spente_oSenzaPassword_oSenzaIndirizzo_nessunInvio() {
        sender(false, "segreta").send(EVENT);
        sender(true, "").send(EVENT);
        sender(true, "segreta").send(new NotificationCreatedEvent(1L, " ", NotificationType.PROPOSAL_SENT, "T1", 34L));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void erroreSmtp_nonInterrompeNulla() {
        NotificationEmailSender sender = sender(true, "segreta");
        doThrow(new MailSendException("connessione rifiutata")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> sender.send(EVENT)).doesNotThrowAnyException();
    }

    @Test
    void lIndirizzoNonFiniscaNeiLog() {
        assertThat(EVENT.toString()).doesNotContain("operatore@nexus.local");
    }
}
