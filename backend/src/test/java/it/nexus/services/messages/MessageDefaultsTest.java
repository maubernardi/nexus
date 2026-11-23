package it.nexus.services.messages;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;
import it.nexus.domain.enumeration.NotificationType;

/** Testi predefiniti dei messaggi (US-1204): uno per ogni parte, con i soli segnaposto ammessi. */
class MessageDefaultsTest {

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void ogniTipoDiNotificaHaIlSuoMessaggio(NotificationType type) {
        MessageKey key = MessageKey.of(type);
        assertThat(key.isNotification()).isTrue();
        assertThat(key.parts()).containsExactly(MessagePart.APP, MessagePart.EMAIL_SUBJECT, MessagePart.EMAIL_BODY);
    }

    @ParameterizedTest
    @EnumSource(MessageKey.class)
    void ogniParteHaUnTestoPredefinitoConISoliSegnapostoAmmessi(MessageKey key) {
        for (MessagePart part : key.parts()) {
            String text = MessageDefaults.get(key, part);
            assertThat(text).as("%s %s", key, part).isNotBlank();
            Matcher m = MessageTexts.PLACEHOLDER.matcher(text);
            while (m.find()) {
                assertThat(key.placeholders()).as("%s %s", key, part).contains(m.group(1));
            }
            if (part == MessagePart.EMAIL_SUBJECT) {
                assertThat(text).doesNotContain("\n").hasSizeLessThanOrEqualTo(100);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void leNotificheCitanoIlNumeroDellaSegnalazione(NotificationType type) {
        MessageKey key = MessageKey.of(type);
        assertThat(MessageDefaults.get(key, MessagePart.APP)).contains("{numero}");
        assertThat(MessageDefaults.get(key, MessagePart.EMAIL_BODY)).contains("{numero}");
    }
}
