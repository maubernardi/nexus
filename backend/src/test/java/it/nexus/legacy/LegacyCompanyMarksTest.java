package it.nexus.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Simboli del database attuale all'inizio del nome (US-514): gli stessi esempi del test della migrazione V52. */
class LegacyCompanyMarksTest {

    @Test
    void stellePolliciERefusi_nomeRipulito() {
        for (LegacyMarksExamples.Example e : LegacyMarksExamples.NAMED) {
            LegacyCompanyMarks marks = LegacyCompanyMarks.of(e.name());
            assertThat(marks.name()).as(e.name()).isEqualTo(e.cleanName());
            assertThat(marks.reliability()).as(e.name()).isEqualTo(e.reliability());
            assertThat(marks.projectInterest()).as(e.name()).isEqualTo(e.projectInterest());
            assertThat(marks.marked()).as(e.name()).isEqualTo(!e.name().equals(e.cleanName()));
        }
    }

    @Test
    void soloSimboli_nomeVuoto() {
        LegacyCompanyMarks marks = LegacyCompanyMarks.of(LegacyMarksExamples.ONLY_MARKS.name());
        assertThat(marks.name()).isNull();
        assertThat(marks.reliability()).isEqualTo(2);
        assertThat(marks.marked()).isTrue();
    }

    @Test
    void nomeAssente() {
        assertThat(LegacyCompanyMarks.of(null)).isEqualTo(new LegacyCompanyMarks(null, null, null, false));
    }
}
