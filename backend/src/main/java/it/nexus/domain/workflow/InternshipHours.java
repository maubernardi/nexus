package it.nexus.domain.workflow;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ore del tirocinio dalla griglia oraria (US-1002): totale settimanale e monte ore complessivo. Il monte ore conta ogni
 * giorno dall'inizio alla fine prevista, estremi compresi, saltando i periodi di sospensione e, se richiesto, le
 * festività nazionali ({@link ItalianHolidays}). Il patrono non si salta: dipende dalla sede dell'azienda.
 */
public final class InternshipHours {

    /** Le due fasce orarie del tirocinio. */
    public record Slots(LocalTime firstStart, LocalTime firstEnd, LocalTime secondStart, LocalTime secondEnd) {

        int firstMinutes() {
            return (int) Duration.between(firstStart, firstEnd).toMinutes();
        }

        int secondMinutes() {
            return (int) Duration.between(secondStart, secondEnd).toMinutes();
        }
    }

    /** Un giorno della griglia: fasce scelte e minuti, nulli = fasce intere. */
    public record Day(DayOfWeek dayOfWeek, boolean firstSlot, boolean secondSlot, Integer minutes) {
    }

    /** Periodo di sospensione, estremi compresi. */
    public record Period(LocalDate from, LocalDate to) {

        boolean includes(LocalDate day) {
            return !day.isBefore(from) && !day.isAfter(to);
        }
    }

    private InternshipHours() {
    }

    /** Durata delle fasce scelte nel giorno: il massimo delle ore che il giorno può indicare. */
    public static int slotMinutes(Slots slots, Day day) {
        return (day.firstSlot() ? slots.firstMinutes() : 0) + (day.secondSlot() ? slots.secondMinutes() : 0);
    }

    /** Minuti del giorno: quelli indicati oppure le fasce scelte intere. */
    public static int dayMinutes(Slots slots, Day day) {
        return day.minutes() != null ? day.minutes() : slotMinutes(slots, day);
    }

    /** Totale settimanale in minuti; nullo senza griglia. */
    public static Integer weeklyMinutes(Slots slots, List<Day> days) {
        return days.isEmpty() ? null : days.stream().mapToInt(d -> dayMinutes(slots, d)).sum();
    }

    /** Monte ore complessivo in minuti; nullo senza date o senza griglia. */
    public static Integer totalMinutes(Slots slots, List<Day> days, LocalDate start, LocalDate end,
            List<Period> suspensions, boolean excludeHolidays) {
        if (start == null || end == null || days.isEmpty()) {
            return null;
        }
        Map<DayOfWeek, Day> byDay = days.stream().collect(Collectors.toMap(Day::dayOfWeek, Function.identity()));
        int total = 0;
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            Day scheduled = byDay.get(day.getDayOfWeek());
            LocalDate current = day;
            if (scheduled == null || suspensions.stream().anyMatch(p -> p.includes(current))
                    || excludeHolidays && ItalianHolidays.isHoliday(day)) {
                continue;
            }
            total += dayMinutes(slots, scheduled);
        }
        return total;
    }
}
