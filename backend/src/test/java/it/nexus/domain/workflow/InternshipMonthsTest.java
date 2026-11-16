package it.nexus.domain.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Mesi arrotondati al mese più vicino, da 15 giorni per eccesso (US-1008), anche sulle convenzioni (US-1011). */
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

    private static InternshipMonths.Span span(String from, String to) {
        return new InternshipMonths.Span(LocalDate.parse(from), LocalDate.parse(to));
    }

    private static int total(String end, InternshipMonths.Span... spans) {
        return InternshipMonths.roundedTotal(List.of(spans), LocalDate.parse(end));
    }

    @Test
    void unaConvenzione_comeIlProgetto() {
        assertThat(total("2027-05-31", span("2026-12-01", "2027-05-31"))).isEqualTo(6);
        assertThat(total("2027-01-15", span("2026-12-01", "2027-01-15"))).isEqualTo(2);
    }

    @Test
    void prorogaConsecutiva_unPeriodoSolo() {
        // 7 mesi e 15 giorni: per eccesso
        assertThat(total("2027-07-15", span("2026-12-01", "2027-05-31"), span("2027-06-01", "2027-07-15")))
                .isEqualTo(8);
    }

    @Test
    void prorogaNonConsecutiva_soloIGiorniCoperti() {
        // 6 mesi, poi dal 15/06 al 30/06 (16 giorni): per eccesso; i giorni tra le due convenzioni non contano
        assertThat(total("2027-06-30", span("2026-12-01", "2027-05-31"), span("2027-06-15", "2027-06-30")))
                .isEqualTo(7);
        // 6 mesi e 10 giorni, poi 10 giorni: 20 giorni restanti, per eccesso
        assertThat(total("2027-07-10", span("2026-12-01", "2027-06-10"), span("2027-07-01", "2027-07-10")))
                .isEqualTo(7);
        // 6 mesi e 5 giorni, poi 5 giorni: 10 giorni restanti, per difetto
        assertThat(total("2027-07-05", span("2026-12-01", "2027-06-05"), span("2027-07-01", "2027-07-05")))
                .isEqualTo(6);
    }

    @Test
    void chiusuraAnticipata_finoAllaFineEffettiva() {
        // chiuso a metà della proroga: la parte dopo la fine effettiva non conta
        assertThat(total("2027-06-20", span("2026-12-01", "2027-05-31"), span("2027-06-01", "2027-09-30")))
                .isEqualTo(7);
        // chiuso prima della proroga
        assertThat(total("2027-04-30", span("2026-12-01", "2027-05-31"), span("2027-06-01", "2027-09-30")))
                .isEqualTo(5);
    }
}
