package it.nexus.services.messages;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;

/**
 * I testi dei messaggi in un momento (US-1204): quelli cambiati dall'ADMIN, altrimenti i predefiniti. Si legge una volta
 * e si usa per comporre più messaggi, per esempio tutto l'elenco delle notifiche.
 */
public final class MessageTexts {

    /** Un segnaposto: un nome tra graffe, come {@code {numero}}. */
    public static final Pattern PLACEHOLDER = Pattern.compile("\\{([^{}]*)}");

    private final Map<MessageKey, Map<MessagePart, String>> custom;

    /** @param custom i testi cambiati dall'ADMIN; per le parti che mancano valgono i predefiniti */
    public MessageTexts(Map<MessageKey, Map<MessagePart, String>> custom) {
        this.custom = custom;
    }

    /** Solo i testi predefiniti: per i test e per chi non ha il database. */
    public static MessageTexts defaults() {
        return new MessageTexts(Map.of());
    }

    /** Il testo predefinito della parte. */
    public static String defaultText(MessageKey key, MessagePart part) {
        return MessageDefaults.get(key, part);
    }

    /** Il testo della parte, con i segnaposto da sostituire. */
    public String template(MessageKey key, MessagePart part) {
        String text = custom.getOrDefault(key, Map.of()).get(part);
        return text != null ? text : MessageDefaults.get(key, part);
    }

    /** Il testo della parte con i segnaposto sostituiti dai valori; un segnaposto senza valore resta com'è. */
    public String render(MessageKey key, MessagePart part, Map<String, ?> values) {
        String template = template(key, part);
        if (template == null) {
            return null;
        }
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            Object value = values.get(m.group(1));
            m.appendReplacement(out, Matcher.quoteReplacement(value == null ? m.group() : value.toString()));
        }
        m.appendTail(out);
        return out.toString();
    }
}
