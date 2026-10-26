package it.nexus.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import it.nexus.domain.CalendarClosure;
import it.nexus.domain.WorkCalendar;
import it.nexus.repository.CalendarClosureRepository;
import it.nexus.repository.WorkCalendarRepository;
import it.nexus.services.impl.WorkingDayServiceImpl;

@ExtendWith(MockitoExtension.class)
class WorkingDayServiceTest {

    @Mock WorkCalendarRepository calendars;
    @Mock CalendarClosureRepository closures;
    @InjectMocks WorkingDayServiceImpl service;

    @BeforeEach
    void setUp() {
        WorkCalendar calendar = Mockito.mock(WorkCalendar.class);
        lenient().when(calendar.getPatron()).thenReturn(MonthDay.of(6, 24));
        lenient().when(calendars.findById(WorkCalendar.SINGLETON_ID)).thenReturn(Optional.of(calendar));
        CalendarClosure summer = new CalendarClosure(LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 21), "Ferie estive");
        lenient().when(closures.findByEndDateGreaterThanEqual(any())).thenReturn(List.of(summer));
        lenient().when(closures.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenAnswer(inv -> summer.includes(inv.getArgument(0)) ? List.of(summer) : List.of());
    }

    @ParameterizedTest(name = "{3}")
    @CsvSource({
            "2026-10-23, 7, 2026-11-03, 'venerdì + 7: salta due fine settimana'",
            "2026-04-02, 3, 2026-04-08, 'salta Pasqua e Pasquetta'",
            "2026-06-22, 3, 2026-06-26, 'salta il patrono (24 giugno)'",
            "2026-08-07, 1, 2026-08-24, 'salta la chiusura estiva (10-21 agosto)'",
            "2026-12-23, 4, 2026-12-30, 'salta Natale e Santo Stefano'",
            "2026-10-26, 4, 2026-10-30, 'promemoria al 4° giorno lavorativo'",
            "2026-10-26, 0, 2026-10-26, 'zero giorni: la data di partenza'"})
    void plusWorkingDays(LocalDate from, int days, LocalDate expected, String description) {
        assertThat(service.plusWorkingDays(from, days)).isEqualTo(expected);
    }

    @Test
    void giorniNegativi_rifiutati() {
        assertThatThrownBy(() -> service.plusWorkingDays(LocalDate.of(2026, 10, 26), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0} lavorativo: {1}")
    @CsvSource({
            "2027-10-04, true",   // il 4 ottobre non è festivo (PO, 24/10)
            "2026-10-24, false",  // sabato
            "2026-10-25, false",  // domenica
            "2026-06-24, false",  // patrono
            "2026-08-12, false",  // chiusura
            "2026-04-06, false",  // Lunedì dell'Angelo
            "2026-10-26, true"})
    void isWorkingDay(LocalDate day, boolean expected) {
        assertThat(service.isWorkingDay(day)).isEqualTo(expected);
    }
}
