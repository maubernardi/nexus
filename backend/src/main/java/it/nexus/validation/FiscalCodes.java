package it.nexus.validation;

import java.time.LocalDate;
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

    /** Lettere dei mesi, da gennaio a dicembre. */
    private static final String MONTHS = "ABCDEHLMPRST";

    /** Lettere che sostituiscono le cifre 0–9 nei codici con omocodia. */
    private static final String OMOCODES = "LMNPQRSTUV";

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

    /**
     * Il codice (già valido) corrisponde alla data di nascita: ultime due cifre dell'anno, mese e giorno, che per le
     * donne è aumentato di 40. Le cifre sostituite dall'omocodia si leggono come le originali.
     */
    public static boolean matchesBirthDate(String value, LocalDate birthDate) {
        String code = value.strip().toUpperCase(Locale.ROOT);
        int day = number(code, 9);
        return number(code, 6) == birthDate.getYear() % 100
                && MONTHS.indexOf(code.charAt(8)) + 1 == birthDate.getMonthValue()
                && (day == birthDate.getDayOfMonth() || day == birthDate.getDayOfMonth() + 40);
    }

    /** Numero di due cifre a partire da {@code start}, con le lettere dell'omocodia riportate a cifre. */
    private static int number(String code, int start) {
        return digit(code.charAt(start)) * 10 + digit(code.charAt(start + 1));
    }

    private static int digit(char c) {
        return Character.isDigit(c) ? c - '0' : OMOCODES.indexOf(c);
    }
}
