package it.nexus.domain.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ItalianHolidaysTest {

    @ParameterizedTest
    @CsvSource({"2024, 2024-03-31", "2025, 2025-04-20", "2026, 2026-04-05", "2027, 2027-03-28", "2028, 2028-04-16",
            "2029, 2029-04-01", "2030, 2030-04-21", "2038, 2038-04-25"})
    void pasqua(int year, LocalDate expected) {
        assertThat(ItalianHolidays.easter(year)).isEqualTo(expected);
    }

    @Test
    void festivitaDel2026_conPasquettaESenzaIl4Ottobre() {
        assertThat(ItalianHolidays.of(2026))
                .hasSize(12)
                .contains(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 6), LocalDate.of(2026, 4, 5),
                        LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 25), LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 6, 2), LocalDate.of(2026, 8, 15), LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26))
                .doesNotContain(LocalDate.of(2026, 10, 4));
    }

    @Test
    void pasquaCheCoincideConIl25Aprile_unaSolaVolta() {
        assertThat(ItalianHolidays.of(2038)).hasSize(11).contains(LocalDate.of(2038, 4, 26));
    }
}
