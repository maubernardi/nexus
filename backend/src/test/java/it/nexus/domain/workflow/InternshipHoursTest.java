package it.nexus.domain.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.nexus.domain.workflow.InternshipHours.Day;
import it.nexus.domain.workflow.InternshipHours.Period;
import it.nexus.domain.workflow.InternshipHours.Slots;

class InternshipHoursTest {

    /** Fasce precompilate: 9–13 e 14–18. */
    private static final Slots SLOTS = new Slots(LocalTime.of(9, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
            LocalTime.of(18, 0));
    private static final LocalDate MONDAY_7_DEC = LocalDate.of(2026, 12, 7);

    private static Day morning(DayOfWeek day) {
        return new Day(day, true, false, null);
    }

    @Test
    void oreDelGiorno_fasceIntereOppureIndicate() {
        assertThat(InternshipHours.dayMinutes(SLOTS, morning(DayOfWeek.MONDAY))).isEqualTo(240);
        assertThat(InternshipHours.dayMinutes(SLOTS, new Day(DayOfWeek.TUESDAY, true, true, null))).isEqualTo(480);
        // 9–13 e 14–18, ma 3 ore (esempio del PO)
        assertThat(InternshipHours.dayMinutes(SLOTS, new Day(DayOfWeek.TUESDAY, true, true, 180))).isEqualTo(180);
        assertThat(InternshipHours.slotMinutes(SLOTS, new Day(DayOfWeek.TUESDAY, true, true, 180))).isEqualTo(480);
        // fasce modificate al minuto
        Slots custom = new Slots(LocalTime.of(8, 30), LocalTime.of(12, 45), LocalTime.of(15, 0), LocalTime.of(19, 0));
        assertThat(InternshipHours.dayMinutes(custom, morning(DayOfWeek.MONDAY))).isEqualTo(255);
    }

    @Test
    void totaleSettimanale() {
        assertThat(InternshipHours.weeklyMinutes(SLOTS, List.of())).isNull();
        assertThat(InternshipHours.weeklyMinutes(SLOTS, List.of(morning(DayOfWeek.MONDAY),
                new Day(DayOfWeek.TUESDAY, true, true, 180)))).isEqualTo(420);
    }

    @Test
    void monteOre_festivitaEscluseOppureNo() {
        List<Day> days = List.of(morning(DayOfWeek.MONDAY), morning(DayOfWeek.TUESDAY));
        LocalDate sunday = MONDAY_7_DEC.plusDays(6);
        // martedì 8 dicembre è festivo
        assertThat(InternshipHours.totalMinutes(SLOTS, days, MONDAY_7_DEC, sunday, List.of(), true)).isEqualTo(240);
        assertThat(InternshipHours.totalMinutes(SLOTS, days, MONDAY_7_DEC, sunday, List.of(), false)).isEqualTo(480);
    }

    @Test
    void monteOre_sospensioniSaltate_estremiCompresi() {
        List<Day> days = List.of(morning(DayOfWeek.MONDAY), morning(DayOfWeek.WEDNESDAY), morning(DayOfWeek.FRIDAY));
        LocalDate end = MONDAY_7_DEC.plusDays(11); // venerdì 18
        List<Period> suspension = List.of(new Period(MONDAY_7_DEC, MONDAY_7_DEC.plusDays(2)));

        assertThat(InternshipHours.totalMinutes(SLOTS, days, MONDAY_7_DEC, end, List.of(), true)).isEqualTo(6 * 240);
        assertThat(InternshipHours.totalMinutes(SLOTS, days, MONDAY_7_DEC, end, suspension, true)).isEqualTo(4 * 240);
    }

    @Test
    void monteOre_pasquettaFestiva() {
        // Pasqua 2027: 28 marzo, Pasquetta lunedì 29
        LocalDate easterMonday = LocalDate.of(2027, 3, 29);
        assertThat(InternshipHours.totalMinutes(SLOTS, List.of(morning(DayOfWeek.MONDAY)), easterMonday,
                easterMonday, List.of(), true)).isZero();
    }

    @Test
    void monteOre_senzaDateOGriglia() {
        assertThat(InternshipHours.totalMinutes(SLOTS, List.of(morning(DayOfWeek.MONDAY)), null, MONDAY_7_DEC,
                List.of(), true)).isNull();
        assertThat(InternshipHours.totalMinutes(SLOTS, List.of(), MONDAY_7_DEC, MONDAY_7_DEC, List.of(), true)).isNull();
    }
}
