package it.nexus.services.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import it.nexus.config.ApplicationProperties;

/** Email del cambio di indirizzo (US-110): codice al nuovo indirizzo, avviso al vecchio. */
class EmailChangeMailerTest {

    // 10:30 UTC = 11:30 a Roma (ora solare)
    private static final Instant EXPIRES_AT = Instant.parse("2026-11-05T10:30:00Z");

    private final JavaMailSenderImpl mailSender = mock(JavaMailSenderImpl.class);

    private EmailChangeMailer mailer(boolean enabled, String password, boolean logOtp) {
        when(mailSender.getPassword()).thenReturn(password);
        ApplicationProperties properties = new ApplicationProperties();
        properties.getMail().setEnabled(enabled);
        properties.getMail().setLogOtp(logOtp);
        return new EmailChangeMailer(mailSender, properties);
    }

    private SimpleMailMessage sent() {
        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        return message.getValue();
    }

    @Test
    void codice_alNuovoIndirizzoConLaScadenzaInOraItaliana() {
        assertThat(mailer(true, "segreta", false).sendCode("nuovo@example.org", "123456", EXPIRES_AT)).isTrue();

        SimpleMailMessage message = sent();
        assertThat(message.getTo()).containsExactly("nuovo@example.org");
        assertThat(message.getFrom()).isEqualTo("NEXUS <noreply@portalenexus.it>");
        assertThat(message.getSubject()).isEqualTo("NEXUS · codice per confermare il nuovo indirizzo email");
        assertThat(message.getText()).contains("    123456").contains("fino alle 11:30 (30 minuti)")
                .contains("ignora questa email");
    }

    @Test
    void codice_erroreSmtp_restituisceFalso() {
        EmailChangeMailer mailer = mailer(true, "segreta", false);
        doThrow(new MailSendException("smtp giù")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThat(mailer.sendCode("nuovo@example.org", "123456", EXPIRES_AT)).isFalse();
    }

    @Test
    void codice_emailSpente_soloInLocaleSiProsegueColCodiceNelLog() {
        assertThat(mailer(false, "", true).sendCode("nuovo@example.org", "123456", EXPIRES_AT)).isTrue();
        assertThat(mailer(false, "", false).sendCode("nuovo@example.org", "123456", EXPIRES_AT)).isFalse();
        assertThat(mailer(true, "", false).sendCode("nuovo@example.org", "123456", EXPIRES_AT)).isFalse();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void avviso_alVecchioIndirizzoSenzaIlNuovo() {
        mailer(true, "segreta", false).sendNotice(new EmailChangedEvent(1L, "vecchio@example.org"));

        SimpleMailMessage message = sent();
        assertThat(message.getTo()).containsExactly("vecchio@example.org");
        assertThat(message.getSubject()).isEqualTo("NEXUS · il tuo indirizzo email è stato cambiato");
        assertThat(message.getText()).contains("Se non sei stato tu, contatta subito l'amministratore")
                .doesNotContain("@");
    }

    @Test
    void avviso_emailSpente_nienteInvio() {
        mailer(false, "", true).sendNotice(new EmailChangedEvent(1L, "vecchio@example.org"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void eventoSenzaIndirizzoNelToString() {
        assertThat(new EmailChangedEvent(7L, "vecchio@example.org").toString()).doesNotContain("vecchio");
    }
}
