package it.nexus.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.MonthDay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.domain.CalendarClosure;
import it.nexus.domain.WorkCalendar;
import it.nexus.repository.CalendarClosureRepository;
import it.nexus.repository.WorkCalendarRepository;

/** Calcolo dei giorni lavorativi sul calendario del database (riga creata dalla migrazione V16). */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class WorkingDayServiceIT {

    @Autowired WorkingDayService workingDays;
    @Autowired WorkCalendarRepository calendars;
    @Autowired CalendarClosureRepository closures;

    @Test
    void patronoEChiusureDalDatabase() {
        // patrono iniziale: 24 giugno 2026 (mercoledì) non è lavorativo
        assertThat(workingDays.isWorkingDay(LocalDate.of(2026, 6, 24))).isFalse();
        assertThat(workingDays.plusWorkingDays(LocalDate.of(2026, 6, 22), 3)).isEqualTo(LocalDate.of(2026, 6, 26));

        // cambiato il patrono e aggiunta una chiusura, il calcolo li segue subito
        WorkCalendar calendar = calendars.findById(WorkCalendar.SINGLETON_ID).orElseThrow();
        calendar.setPatron(MonthDay.of(6, 25));
        calendars.saveAndFlush(calendar);
        closures.saveAndFlush(new CalendarClosure(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2), "Ponte"));

        assertThat(workingDays.isWorkingDay(LocalDate.of(2026, 6, 24))).isTrue();
        assertThat(workingDays.plusWorkingDays(LocalDate.of(2026, 6, 22), 3)).isEqualTo(LocalDate.of(2026, 6, 26));
        assertThat(workingDays.plusWorkingDays(LocalDate.of(2026, 10, 23), 7)).isEqualTo(LocalDate.of(2026, 11, 4));
    }
}
