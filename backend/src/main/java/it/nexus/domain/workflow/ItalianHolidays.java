package it.nexus.domain.workflow;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Festività nazionali italiane (US-703), calcolate per ogni anno: le fisse più Pasqua e Lunedì dell'Angelo. Il 4 ottobre
 * non è considerato festivo (decisione del PO, 24/10/2026). Patrono e chiusure sono nel calendario unico (US-107).
 */
public final class ItalianHolidays {

    private static final List<MonthDay> FIXED = List.of(
            MonthDay.of(1, 1),    // Capodanno
            MonthDay.of(1, 6),    // Epifania
            MonthDay.of(4, 25),   // Festa della Liberazione
            MonthDay.of(5, 1),    // Festa del Lavoro
            MonthDay.of(6, 2),    // Festa della Repubblica
            MonthDay.of(8, 15),   // Ferragosto
            MonthDay.of(11, 1),   // Ognissanti
            MonthDay.of(12, 8),   // Immacolata
            MonthDay.of(12, 25),  // Natale
            MonthDay.of(12, 26)); // Santo Stefano

    private ItalianHolidays() {
    }

    /** Festività nazionali dell'anno indicato. */
    public static Set<LocalDate> of(int year) {
        LocalDate easter = easter(year);
        return Stream.concat(FIXED.stream().map(d -> d.atYear(year)), Stream.of(easter, easter.plusDays(1)))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static boolean isHoliday(LocalDate day) {
        return of(day.getYear()).contains(day);
    }

    /** Domenica di Pasqua nel calendario gregoriano (algoritmo di Meeus/Jones/Butcher). */
    public static LocalDate easter(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(year, month, day);
    }
}
