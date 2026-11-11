package it.nexus.domain.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Mesi arrotondati al mese più vicino, da 15 giorni per eccesso (US-1008). */
class InternshipMonthsTest {

    private static int months(String from, String to) {
        return InternshipMonths.rounded(LocalDate.parse(from), LocalDate.parse(to));
    }

    @Test
    void mesiInteri() {
        assertThat(months("2026-12-01", "2027-02-28")).isEqualTo(3);
        assertThat(months("2026-12-01", "2027-05-31")).isEqualTo(6);
        assertThat(months("2026-12-15", "2027-12-14")).isEqualTo(12);
    }

    @Test
    void quindiciGiorniPerEccesso() {
        // 1 mese e 15 giorni
        assertThat(months("2026-12-01", "2027-01-15")).isEqualTo(2);
        // 1 mese e 13 giorni (febbraio)
        assertThat(months("2027-01-16", "2027-02-28")).isEqualTo(1);
        // 1 mese e 14 giorni
        assertThat(months("2026-12-01", "2027-01-14")).isEqualTo(1);
    }

    @Test
    void periodiBrevi() {
        assertThat(months("2026-12-01", "2026-12-15")).isEqualTo(1);
        assertThat(months("2026-12-01", "2026-12-14")).isZero();
        assertThat(months("2026-12-01", "2026-12-01")).isZero();
    }
}
