package it.nexus.services.mail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import it.nexus.domain.enumeration.NotificationType;

/** Testi delle email delle notifiche (US-1202): uno per tipo, con il solo numero della segnalazione. */
class NotificationEmailTest {

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void ogniTipoHaIlSuoTesto_conNumeroECollegamento(NotificationType type) {
        NotificationEmail email = NotificationEmail.of(type, "0S2CB3NATG1AY", 34L, "https://portalenexus.it");

        assertThat(email.subject()).isEqualTo("NEXUS · segnalazione n. 34");
        assertThat(email.body())
                .contains("segnalazione n. 34")
                .contains("Apri la segnalazione: https://portalenexus.it/segnalazioni/0S2CB3NATG1AY")
                .doesNotContain("%d", "{{");
    }

    @Test
    void testoDellaNotificaInApp() {
        assertThat(NotificationEmail.of(NotificationType.PROPOSAL_SENT, "T1", 7L, "https://portalenexus.it").body())
                .startsWith("C'è una proposta di tirocinio per la segnalazione n. 7: valutala e accettala.\n")
                .endsWith("Trovi tutte le notifiche anche nell'app, alla voce Notifiche.\n");
    }
}
