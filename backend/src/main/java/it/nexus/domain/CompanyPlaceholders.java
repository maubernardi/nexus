package it.nexus.domain;

/**
 * Segnaposto dei dati che il database attuale non ha (US-508, PO 19/11): li scrive solo l'importazione (US-1309), il
 * modulo dell'azienda li rifiuta e la prima modifica li corregge.
 *
 * <p>Si riconoscono in modo stretto (esattamente come li scrive l'importazione) e si rifiutano in modo largo (anche con
 * spazi e maiuscole diverse): così un'azienda salvata dal modulo non risulta mai da completare.
 */
public final class CompanyPlaceholders {

    /** Partita IVA mancante. */
    public static final String VAT_CODE = "00000000000";

    /** Inizio del nome mancante, seguito dal numero dell'azienda nel database attuale. */
    public static final String NAME_PREFIX = "Azienda senza nome n. ";

    /** Per {@code @Pattern}: qualunque partita IVA tranne il segnaposto, anche con spazi intorno. */
    public static final String NOT_VAT_CODE_REGEX = "(?!\\s*" + VAT_CODE + "\\s*$).*";

    /** Per {@code @Pattern}: qualunque nome che non cominci come il segnaposto, senza distinguere le maiuscole. */
    public static final String NOT_NAME_REGEX = "(?is)(?!\\s*azienda senza nome n\\.).*";

    private CompanyPlaceholders() {
    }

    /** Nome dell'azienda senza nome con il numero {@code legacyNumber} del database attuale. */
    public static String name(long legacyNumber) {
        return NAME_PREFIX + legacyNumber;
    }

    public static boolean isVatCode(String vatCode) {
        return VAT_CODE.equals(vatCode);
    }

    public static boolean isName(String name) {
        return name != null && name.startsWith(NAME_PREFIX);
    }
}
