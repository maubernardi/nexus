package it.nexus.legacy;

/**
 * Id fissi delle righe importate dal database attuale (US-1309): ricavati dai numeri di Access, così una seconda
 * importazione non duplica nulla ({@code ON CONFLICT (id) DO NOTHING}). Stanno sotto gli id generati oggi, come quelli
 * delle voci iniziali di V46 e V47.
 */
public final class LegacyIds {

    /** Autore delle righe importate; il diario lo mostra come «Database attuale». */
    public static final String AUTHOR = "database-attuale";

    static final long COMPANY = 893700000010000000L;
    static final long CONTACT = 893700000020000000L;
    static final long NOTE = 893700000030000000L;
    static final long AUDIT = 893700000040000000L;
    static final long CALL = 893700000050000000L;
    /** Voci iniziali dei cataloghi (V46, V47): base + numero di Access. */
    static final long BUSINESS_CATEGORY = 893700000000000400L;
    static final long CONTACT_ROLE = 893700000000000500L;

    private LegacyIds() {
    }

    static long company(int number) {
        return COMPANY + number;
    }

    static long contact(int companyNumber, int position) {
        return CONTACT + companyNumber * 10L + position;
    }

    /** {@code n}: 0 = nota di Access, 1 = recapiti che non stanno nei campi. */
    static long note(int companyNumber, int n) {
        return NOTE + companyNumber * 10L + n;
    }

    static long audit(int companyNumber) {
        return AUDIT + companyNumber;
    }

    static long call(int number) {
        return CALL + number;
    }
}
