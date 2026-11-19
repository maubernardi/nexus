package it.nexus.legacy;

import java.text.Normalizer;
import java.util.Locale;

/** Testi del database attuale: spazi tolti, vuoto = assente; forma normalizzata per i confronti. */
final class LegacyText {

    private LegacyText() {
    }

    static String clean(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }

    /** Senza accenti, in maiuscolo, punteggiatura e spazi ridotti a uno spazio: «Sant'Agata» → «SANT AGATA». */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return ascii.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").strip();
    }
}
