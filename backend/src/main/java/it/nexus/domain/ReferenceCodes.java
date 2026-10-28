package it.nexus.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Codice di una voce di catalogo ricavato dal nome (es. «Addetto vendite» → {@code ADDETTO_VENDITE}): maiuscole, cifre
 * e underscore ({@code ck_*_code}), al più 30 caratteri, con un suffisso numerico se è già usato.
 */
public final class ReferenceCodes {

    private static final int BASE_LENGTH = 26;

    private ReferenceCodes() {
    }

    public static String fromName(String name, String fallback, Predicate<String> taken) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        base = base.isEmpty() ? fallback : base.substring(0, Math.min(base.length(), BASE_LENGTH));
        String code = base;
        for (int i = 2; taken.test(code); i++) {
            code = base + "_" + i;
        }
        return code;
    }
}
