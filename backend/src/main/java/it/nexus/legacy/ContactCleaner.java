package it.nexus.legacy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recapiti del database attuale (US-1309) ridotti al formato dei campi di NEXUS. Quello che non ci sta (testi accanto
 * al numero, indirizzi non validi) torna come testo originale, per la nota del diario.
 */
final class ContactCleaner {

    /** Come il modulo dell'azienda: cifre, spazi e {@code + ( ) . - /}. */
    private static final Pattern PHONE = Pattern.compile("[0-9 +().\\-/]+");
    /**
     * Più severo del modulo e di {@code @Email}: quello che passa qui si salva senza errori. Niente barre (resti di
     * {@code mailto://}), dominio con lettere, cifre e trattini, estensione di sole lettere.
     */
    private static final String LOCAL_CHARS = "[A-Za-z0-9!#$%&'*+=?^_`{|}~-]+";
    private static final Pattern EMAIL = Pattern.compile(LOCAL_CHARS + "(\\." + LOCAL_CHARS + ")*"
            + "@([A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}");
    private static final int PHONE_LENGTH = 30;
    private static final int EMAIL_LENGTH = 254;
    private static final int MIN_PHONE_DIGITS = 6;

    /** Telefono per il campo (assente se non c'è un numero) e testo originale, se nel campo non c'è tutto. */
    record Phone(String value, String original) {
    }

    /** Indirizzi validi, senza doppioni, e testo originale, se ha altro oltre agli indirizzi. */
    record Emails(List<String> valid, String original) {
    }

    private ContactCleaner() {
    }

    static Phone phone(String raw) {
        String text = LegacyText.clean(raw);
        if (text == null) {
            return new Phone(null, null);
        }
        if (PHONE.matcher(text).matches() && text.length() <= PHONE_LENGTH) {
            return new Phone(text, null);
        }
        Matcher leading = PHONE.matcher(text.lines().findFirst().orElse(""));
        String value = null;
        if (leading.lookingAt()) {
            value = leading.group().replaceAll("[\\s(\\-/.+]+$", "").strip();
            if (value.chars().filter(Character::isDigit).count() < MIN_PHONE_DIGITS || value.length() > PHONE_LENGTH) {
                value = null;
            }
        }
        return new Phone(value, text);
    }

    /**
     * Gli indirizzi di un campo email di Access, che spesso è un collegamento ({@code testo#http://indirizzo#}). Il
     * testo originale torna (senza la parte del collegamento) se ha parole che non sono indirizzi validi.
     */
    static Emails emails(String raw) {
        String text = LegacyText.clean(raw);
        if (text == null) {
            return new Emails(List.of(), null);
        }
        Map<String, String> valid = new LinkedHashMap<>();
        boolean other = false;
        for (String token : text.split("[#\\s,;]+")) {
            // schema del collegamento (Access ha salvato quasi tutte le email come «http://indirizzo»), punteggiatura
            // attorno all'indirizzo e punto finale
            String candidate = token.replaceFirst("(?i)^(mailto:|https?:)?/*", "").replaceAll("^[<(\"']+|[>)\"'.]+$", "");
            if (candidate.isEmpty()) {
                continue;
            }
            if (EMAIL.matcher(candidate).matches() && candidate.length() <= EMAIL_LENGTH) {
                valid.putIfAbsent(candidate.toLowerCase(Locale.ROOT), candidate);
            } else {
                other = true;
            }
        }
        String shown = text.contains("#") ? LegacyText.clean(text.substring(0, text.indexOf('#'))) : text;
        return new Emails(new ArrayList<>(valid.values()), other || valid.isEmpty() ? shown : null);
    }
}
