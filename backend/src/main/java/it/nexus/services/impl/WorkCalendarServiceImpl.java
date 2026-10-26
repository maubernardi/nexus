package it.nexus.services.impl;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.CalendarClosure;
import it.nexus.domain.WorkCalendar;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.ClosureFormDTO;
import it.nexus.domain.dto.PatronFormDTO;
import it.nexus.domain.dto.WorkCalendarDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CalendarClosureRepository;
import it.nexus.repository.WorkCalendarRepository;
import it.nexus.services.AuditService;
import it.nexus.services.WorkCalendarService;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkCalendarServiceImpl implements WorkCalendarService {

    private final WorkCalendarRepository calendarRepository;
    private final CalendarClosureRepository closureRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public WorkCalendarDTO get() {
        return toDto(calendar());
    }

    @Override
    public WorkCalendarDTO updatePatron(PatronFormDTO dto) {
        MonthDay patron;
        try {
            patron = MonthDay.of(dto.month(), dto.day());
        } catch (DateTimeException e) {
            throw new FieldValidationException("day", "Il giorno non esiste in quel mese");
        }
        WorkCalendar calendar = calendar();
        if (calendar.getVersion() != dto.version()) {
            throw new ConflictException("Il calendario è stato modificato nel frattempo: ricarica la pagina e riprova");
        }
        MonthDay before = calendar.getPatron();
        if (!before.equals(patron)) {
            calendar.setPatron(patron);
            calendarRepository.saveAndFlush(calendar);
            auditService.record(AuditEntityType.CALENDAR, (long) WorkCalendar.SINGLETON_ID, "PATRON_UPDATE",
                    AuditChanges.of().value("patron", before.toString(), patron.toString()), null);
        }
        return toDto(calendar);
    }

    @Override
    public WorkCalendarDTO addClosure(ClosureFormDTO dto) {
        LocalDate end = dto.endDate() == null ? dto.startDate() : dto.endDate();
        if (end.isBefore(dto.startDate())) {
            throw new FieldValidationException("endDate", "La fine non può precedere l'inizio");
        }
        CalendarClosure saved = closureRepository.saveAndFlush(
                new CalendarClosure(dto.startDate(), end, dto.description().strip()));
        auditService.record(AuditEntityType.CALENDAR, (long) WorkCalendar.SINGLETON_ID, "CLOSURE_ADD",
                closureValues(saved, false), null);
        return toDto(calendar());
    }

    @Override
    public WorkCalendarDTO removeClosure(String closureId) {
        CalendarClosure closure = Optional.ofNullable(TsidMapper.toInternal(closureId))
                .flatMap(closureRepository::findById)
                .orElseThrow(() -> new NotFoundException("Chiusura non trovata"));
        closureRepository.delete(closure);
        auditService.record(AuditEntityType.CALENDAR, (long) WorkCalendar.SINGLETON_ID, "CLOSURE_REMOVE",
                closureValues(closure, true), null);
        return toDto(calendar());
    }

    /** Valori della chiusura nell'audit: "dopo" se aggiunta, "prima" se rimossa (non sono dati personali). */
    private static AuditChanges closureValues(CalendarClosure c, boolean removed) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("closureId", TsidMapper.toExternal(c.getId()));
        values.put("startDate", c.getStartDate().toString());
        values.put("endDate", c.getEndDate().toString());
        values.put("description", c.getDescription());
        AuditChanges changes = AuditChanges.of();
        values.forEach((field, value) -> changes.value(field, removed ? value : null, removed ? null : value));
        return changes;
    }

    private WorkCalendar calendar() {
        return calendarRepository.findById(WorkCalendar.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Calendario assente: manca la riga della migrazione V16"));
    }

    private WorkCalendarDTO toDto(WorkCalendar calendar) {
        return new WorkCalendarDTO(calendar.getPatronDay(), calendar.getPatronMonth(), calendar.getVersion(),
                closureRepository.findAllByOrderByStartDateAscEndDateAsc().stream()
                        .map(c -> new WorkCalendarDTO.Closure(TsidMapper.toExternal(c.getId()), c.getStartDate(),
                                c.getEndDate(), c.getDescription()))
                        .toList());
    }
}
