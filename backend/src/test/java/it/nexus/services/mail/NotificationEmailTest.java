package it.nexus.services.mail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.services.messages.MessageTexts;

/**
 * Testi delle email delle notifiche (US-1202): uno per tipo, con il solo numero della segnalazione; oggetto con la parte
 * distintiva del tipo e testi modificabili dall'ADMIN (US-1204).
 */
class NotificationEmailTest {

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void ogniTipoHaIlSuoTesto_conNumeroECollegamento(NotificationType type) {
        NotificationEmail email = NotificationEmail.of(MessageTexts.defaults(), type, "0S2CB3NATG1AY", 34L,
                null, "https://portalenexus.it");

        assertThat(email.subject()).startsWith("PORTALE NEXUS - Segnalazione n. 34 - ")
                .isEqualTo("PORTALE NEXUS - Segnalazione n. 34 - "
                        + MessageTexts.defaultText(MessageKey.of(type), MessagePart.EMAIL_SUBJECT));
        assertThat(email.body())
                .contains("segnalazione n. 34")
                .contains("Apri la segnalazione: https://portalenexus.it/segnalazioni/0S2CB3NATG1AY")
                .doesNotContain("%d", "{{", "{numero}")
                .endsWith("\n\nPortale Nexus\n");
    }

    @Test
    void testoDellaNotificaInApp() {
        NotificationEmail email = NotificationEmail.of(MessageTexts.defaults(), NotificationType.PROPOSAL_SENT, "T1",
                7L, null, "https://portalenexus.it");
        assertThat(email.subject()).isEqualTo("PORTALE NEXUS - Segnalazione n. 7 - Nuova proposta di tirocinio");
        assertThat(email.body())
                .startsWith("C'è una proposta di tirocinio per la segnalazione n. 7: valutala e accettala.\n")
                .endsWith("Trovi tutte le notifiche anche nell'app, alla voce Notifiche.\n\nPortale Nexus\n");
    }

    @Test
    void testiDellAdmin_oggettoETestoCambiati_partiFisseUguali() {
        MessageTexts texts = new MessageTexts(Map.of(MessageKey.PROPOSAL_SENT, Map.of(
                MessagePart.EMAIL_SUBJECT, "Proposta per il n. {numero}",
                MessagePart.EMAIL_BODY, "Ciao, guarda la proposta della segnalazione {numero}.")));

        NotificationEmail email = NotificationEmail.of(texts, NotificationType.PROPOSAL_SENT, "T1", 7L,
                null, "https://portalenexus.it");

        assertThat(email.subject()).isEqualTo("PORTALE NEXUS - Segnalazione n. 7 - Proposta per il n. 7");
        assertThat(email.body())
                .startsWith("Ciao, guarda la proposta della segnalazione 7.\n")
                .contains("Apri la segnalazione: https://portalenexus.it/segnalazioni/T1")
                .endsWith("\n\nPortale Nexus\n");
    }

    @Test
    void propostaDiNaveScuola_collegamentoAllaSchedaDellAzienda() {
        NotificationEmail email = NotificationEmail.of(MessageTexts.defaults(), NotificationType.TRAINING_SHIP_PROPOSED,
                "T1", 7L, "0S2CB3NATG1AZ", "https://portalenexus.it");

        assertThat(email.subject()).isEqualTo("PORTALE NEXUS - Segnalazione n. 7 - Proposta di nave scuola");
        assertThat(email.body())
                .startsWith("Nel report di restituzione della segnalazione n. 7 il Tutor propone l'azienda")
                .contains("Apri la scheda dell'azienda: https://portalenexus.it/aziende/0S2CB3NATG1AZ")
                .doesNotContain("/segnalazioni/")
                .endsWith("\n\nPortale Nexus\n");
    }
}
