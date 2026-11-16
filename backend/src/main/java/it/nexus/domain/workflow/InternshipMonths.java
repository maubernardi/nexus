package it.nexus.domain.workflow;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Durata in mesi per i documenti (US-1008, regola di US-1011): dal primo giorno al giorno dopo l'ultimo, in mesi interi,
 * più uno se restano 15 giorni o più. Le sospensioni sono comprese.
 */
public final class InternshipMonths {

    private static final int HALF_MONTH_DAYS = 15;

    private static final int MONTH_DAYS = 30;

    /** Un periodo coperto da una convenzione, estremi compresi. */
    public record Span(LocalDate from, LocalDate to) {
    }

    private InternshipMonths() {
    }

    /** Mesi arrotondati al mese più vicino, estremi compresi; {@code to} non precede {@code from}. */
    public static int rounded(LocalDate from, LocalDate to) {
        Period period = Period.between(from, to.plusDays(1));
        int months = (int) period.toTotalMonths();
        return period.getDays() >= HALF_MONTH_DAYS ? months + 1 : months;
    }

    /**
     * Mesi del tirocinio sulle sue convenzioni (US-1011, D4), fino a {@code end}: le convenzioni consecutive diventano un
     * periodo solo; mesi interi e giorni restanti si sommano, ogni 30 giorni fanno un mese e un resto di 15 giorni o
     * più vale un mese in più. Con un periodo solo è {@link #rounded}. I periodi dopo {@code end} non contano.
     */
    public static int roundedTotal(List<Span> spans, LocalDate end) {
        List<Span> merged = new ArrayList<>();
        for (Span span : spans.stream().sorted(Comparator.comparing(Span::from)).toList()) {
            LocalDate to = span.to().isAfter(end) ? end : span.to();
            if (to.isBefore(span.from())) {
                continue;
            }
            Span last = merged.isEmpty() ? null : merged.getLast();
            if (last != null && !span.from().isAfter(last.to().plusDays(1))) {
                merged.set(merged.size() - 1, new Span(last.from(), to.isAfter(last.to()) ? to : last.to()));
            } else {
                merged.add(new Span(span.from(), to));
            }
        }
        int months = 0;
        int days = 0;
        for (Span span : merged) {
            Period period = Period.between(span.from(), span.to().plusDays(1));
            months += (int) period.toTotalMonths();
            days += period.getDays();
        }
        months += days / MONTH_DAYS;
        return days % MONTH_DAYS >= HALF_MONTH_DAYS ? months + 1 : months;
    }
}
