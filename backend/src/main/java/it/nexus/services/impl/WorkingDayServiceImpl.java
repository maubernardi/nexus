package it.nexus.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.CalendarClosure;
import it.nexus.domain.WorkCalendar;
import it.nexus.domain.workflow.ItalianHolidays;
import it.nexus.repository.CalendarClosureRepository;
import it.nexus.repository.WorkCalendarRepository;
import it.nexus.services.WorkingDayService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkingDayServiceImpl implements WorkingDayService {

    private final WorkCalendarRepository calendarRepository;
    private final CalendarClosureRepository closureRepository;

    @Override
    public boolean isWorkingDay(LocalDate day) {
        return isWorkingDay(day, patron(),
                closureRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(day, day));
    }

    @Override
    public LocalDate plusWorkingDays(LocalDate from, int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Numero di giorni lavorativi negativo: " + days);
        }
        MonthDay patron = patron();
        // le chiusure che iniziano entro la data più lontana possibile non si conoscono in anticipo: si leggono quelle
        // ancora in corso o future, poche righe
        List<CalendarClosure> closures = closureRepository.findByEndDateGreaterThanEqual(from);
        LocalDate day = from;
        int counted = 0;
        while (counted < days) {
            day = day.plusDays(1);
            if (isWorkingDay(day, patron, closures)) {
                counted++;
            }
        }
        return day;
    }

    private static boolean isWorkingDay(LocalDate day, MonthDay patron, List<CalendarClosure> closures) {
        DayOfWeek dow = day.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY
                && dow != DayOfWeek.SUNDAY
                && !ItalianHolidays.isHoliday(day)
                && !MonthDay.from(day).equals(patron)
                && closures.stream().noneMatch(c -> c.includes(day));
    }

    private MonthDay patron() {
        return calendarRepository.findById(WorkCalendar.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Calendario assente: manca la riga della migrazione V16"))
                .getPatron();
    }
}
