package it.nexus.validation;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Controllo del codice fiscale di persona fisica: struttura ufficiale (anche con omocodia) e carattere di controllo.
 * Usato da {@link FiscalCode} e da chi deve verificare un codice fuori da un DTO.
 */
public final class FiscalCodes {

    private static final Pattern STRUCTURE = Pattern.compile(
            "^[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]$");

    /** Valori dei caratteri nelle posizioni dispari (1ª, 3ª, …) per il carattere di controllo. */
    private static final int[] ODD_DIGITS = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21};
    private static final int[] ODD_LETTERS = {1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16,
            10, 22, 25, 24, 23};

    private FiscalCodes() {
    }

    public static boolean isValid(String value) {
        String code = value.strip().toUpperCase(Locale.ROOT);
        if (!STRUCTURE.matcher(code).matches()) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            char c = code.charAt(i);
            boolean digit = Character.isDigit(c);
            int index = digit ? c - '0' : c - 'A';
            // posizioni dispari per chi conta da 1: indici pari
            sum += i % 2 == 0 ? (digit ? ODD_DIGITS[index] : ODD_LETTERS[index]) : index;
        }
        return code.charAt(15) == (char) ('A' + sum % 26);
    }
}
