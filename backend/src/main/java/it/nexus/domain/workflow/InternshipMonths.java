package it.nexus.domain.workflow;

import java.time.LocalDate;
import java.time.Period;

/**
 * Durata in mesi per i documenti (US-1008, regola di US-1011): dal primo giorno al giorno dopo l'ultimo, in mesi interi,
 * più uno se restano 15 giorni o più. Le sospensioni sono comprese.
 */
public final class InternshipMonths {

    private static final int HALF_MONTH_DAYS = 15;

    private InternshipMonths() {
    }

    /** Mesi arrotondati al mese più vicino, estremi compresi; {@code to} non precede {@code from}. */
    public static int rounded(LocalDate from, LocalDate to) {
        Period period = Period.between(from, to.plusDays(1));
        int months = (int) period.toTotalMonths();
        return period.getDays() >= HALF_MONTH_DAYS ? months + 1 : months;
    }
}
