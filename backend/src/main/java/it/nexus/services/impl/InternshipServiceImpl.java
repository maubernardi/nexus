package it.nexus.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyAtecoCode;
import it.nexus.domain.Internship;
import it.nexus.domain.InternshipType;
import it.nexus.domain.Ticket;
import it.nexus.domain.UserProject;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.AtecoCodeDTO;
import it.nexus.domain.dto.AtecoCodeFormDTO;
import it.nexus.domain.dto.CompanyTutorDTO;
import it.nexus.domain.dto.InternshipDTO;
import it.nexus.domain.dto.InternshipFormDTO;
import it.nexus.domain.dto.LegalRepresentativeDTO;
import it.nexus.domain.dto.LegalRepresentativeFormDTO;
import it.nexus.domain.dto.ScheduleDayDTO;
import it.nexus.domain.dto.SuspensionDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.workflow.InternshipHours;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompanyAtecoCodeRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CompanyLegalDataService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.InternshipService;
import it.nexus.validation.FiscalCodes;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InternshipServiceImpl implements InternshipService {

    /**
     * Stati in cui c'è la scheda: dall'appuntamento fissato in poi. Il Tutor la modifica in qualunque momento, anche a
     * tirocinio avviato (PO, 29/10).
     */
    private static final Set<TicketStatus> WITH_INTERNSHIP = EnumSet.of(TicketStatus.APPUNTAMENTO, TicketStatus.IN_TIROCINIO,
            TicketStatus.FORM_RESTITUZIONE);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ITALIAN);
    /** Totale settimanale massimo, segnaposto da confermare con i moduli ufficiali (Q5). */
    private static final int MAX_WEEKLY_MINUTES = 40 * 60;

    private final InternshipRepository internshipRepository;
    private final TicketRepository ticketRepository;
    private final UserProjectRepository userProjectRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final CompanyAtecoCodeRepository atecoCodeRepository;
    private final CompanyLegalDataService companyLegalDataService;
    private final CurrentAppUserService currentAppUserService;
    private final AuditService auditService;
    private final ReferenceMapper referenceMapper;

    @Override
    @Transactional(readOnly = true)
    public InternshipDTO get(String ticketId) {
        Ticket ticket = internshipTicket(ticketId);
        // scheda mai salvata: un'istanza non persistita porta le fasce precompilate
        return toDto(ticket, internshipRepository.findByTicketId(ticket.getId())
                .orElseGet(() -> new Internship(ticket)));
    }

    @Override
    public InternshipDTO save(String ticketId, InternshipFormDTO dto) {
        Ticket ticket = internshipTicket(ticketId);
        Optional<Internship> existing = internshipRepository.findByTicketId(ticket.getId());
        if (existing.isPresent() != (dto.version() != null)
                || existing.filter(i -> i.getVersion() != dto.version()).isPresent()) {
            throw new ConflictException(
                    "La scheda del tirocinio è stata modificata nel frattempo: ricarica la pagina e riprova");
        }
        Internship internship = existing.orElseGet(() -> new Internship(ticket));
        InternshipType type = internshipType(dto.internshipTypeId(), internship.getInternshipType());
        CompanyAtecoCode ateco = atecoCode(dto.atecoCodeId(), company(ticket));
        if (dto.startDate() != null && dto.expectedEndDate() != null
                && dto.expectedEndDate().isBefore(dto.startDate())) {
            throw new FieldValidationException("expectedEndDate", "La fine prevista non può precedere l'inizio");
        }
        checkSuspensions(dto);
        checkSchedule(dto);
        String fiscalCode = blankToNull(dto.fiscalCode());
        fiscalCode = fiscalCode == null ? null : fiscalCode.toUpperCase(Locale.ROOT);
        if (fiscalCode != null && dto.birthDate() != null && !FiscalCodes.matchesBirthDate(fiscalCode, dto.birthDate())) {
            throw new FieldValidationException("fiscalCode", "Il codice fiscale non corrisponde alla data di nascita");
        }
        List<CompanyTutorDTO> tutors = dto.companyTutors().stream()
                .map(t -> new CompanyTutorDTO(t.fullName().strip(), blankToNull(t.contact())))
                .toList();
        List<SuspensionDTO> suspensions = dto.suspensions().stream()
                .sorted(Comparator.comparing(SuspensionDTO::from))
                .toList();
        List<ScheduleDayDTO> schedule = dto.schedule().stream()
                .sorted(Comparator.comparing(ScheduleDayDTO::dayOfWeek))
                .toList();

        // dati del tirocinio con i valori; dati personali (beneficiario, tutor aziendali, note) solo come "cambiato"
        AuditChanges changes = AuditChanges.of()
                .value("internshipType", code(internship.getInternshipType()), code(type))
                .value("startDate", text(internship.getStartDate()), text(dto.startDate()))
                .value("expectedEndDate", text(internship.getExpectedEndDate()), text(dto.expectedEndDate()))
                .value("slots", slotsText(internship.getFirstSlotStart(), internship.getFirstSlotEnd(),
                        internship.getSecondSlotStart(), internship.getSecondSlotEnd()),
                        slotsText(dto.firstSlotStart(), dto.firstSlotEnd(), dto.secondSlotStart(), dto.secondSlotEnd()))
                .value("schedule", scheduleText(schedule(internship)), scheduleText(schedule))
                .value("excludeHolidays", internship.isExcludeHolidays(), dto.excludeHolidays())
                .value("site", internship.getSite(), blankToNull(dto.site()))
                .value("atecoCode", atecoText(internship.getAtecoCode()), atecoText(ateco))
                .value("suspensions", periods(suspensions(internship)), periods(suspensions))
                .redactedIfChanged("companyTutors", tutors(internship), tutors)
                .redactedIfChanged("fiscalCode", internship.getFiscalCode(), fiscalCode)
                .redactedIfChanged("birthDate", internship.getBirthDate(), dto.birthDate())
                .redactedIfChanged("birthPlace", internship.getBirthPlace(), blankToNull(dto.birthPlace()))
                .redactedIfChanged("residence", internship.getResidence(), blankToNull(dto.residence()))
                .redactedIfChanged("notes", internship.getNotes(), blankToNull(dto.notes()));

        internship.setInternshipType(type);
        internship.setStartDate(dto.startDate());
        internship.setExpectedEndDate(dto.expectedEndDate());
        internship.setFirstSlotStart(dto.firstSlotStart());
        internship.setFirstSlotEnd(dto.firstSlotEnd());
        internship.setSecondSlotStart(dto.secondSlotStart());
        internship.setSecondSlotEnd(dto.secondSlotEnd());
        internship.setExcludeHolidays(dto.excludeHolidays());
        internship.setSite(blankToNull(dto.site()));
        internship.setAtecoCode(ateco);
        internship.setFiscalCode(fiscalCode);
        internship.setBirthDate(dto.birthDate());
        internship.setBirthPlace(blankToNull(dto.birthPlace()));
        internship.setResidence(blankToNull(dto.residence()));
        internship.setNotes(blankToNull(dto.notes()));
        replaceRows(internship, tutors, suspensions, schedule);
        Internship saved = internshipRepository.saveAndFlush(internship);
        if (!changes.asMap().isEmpty()) {
            auditService.record(AuditEntityType.TICKET, ticket.getId(), "INTERNSHIP_SAVE", changes, null);
        }
        return toDto(ticket, saved);
    }

    @Override
    public LegalRepresentativeDTO addLegalRepresentative(String ticketId, LegalRepresentativeFormDTO dto) {
        Company company = company(internshipTicket(ticketId));
        String companyId = TsidMapper.toExternal(company.getId());
        if (companyLegalDataService.legalRepresentative(companyId).isPresent()) {
            throw new ConflictException("Il legale rappresentante di %s è già indicato: per correggerlo rivolgiti al "
                    + "Call Center", company.getName());
        }
        return companyLegalDataService.saveLegalRepresentative(companyId, new LegalRepresentativeFormDTO(
                dto.firstName(), dto.lastName(), dto.fiscalCode(), dto.birthDate(), dto.birthPlace(), dto.residence(),
                null));
    }

    @Override
    public AtecoCodeDTO addAtecoCode(String ticketId, AtecoCodeFormDTO dto) {
        Company company = company(internshipTicket(ticketId));
        return companyLegalDataService.addAtecoCode(TsidMapper.toExternal(company.getId()), dto);
    }

    @Override
    public boolean clearCompanyData(Ticket ticket) {
        return internshipRepository.findByTicketId(ticket.getId())
                .filter(i -> i.getInternshipType() != null || i.getStartDate() != null || i.getExpectedEndDate() != null
                        || i.getSite() != null || i.getAtecoCode() != null || i.getNotes() != null
                        || !i.getCompanyTutors().isEmpty() || !i.getSuspensions().isEmpty()
                        || !i.getScheduleDays().isEmpty())
                .map(i -> {
                    i.clearCompanyData();
                    internshipRepository.saveAndFlush(i);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Segnalazione del Tutor corrente. Quella di un collega di progetto la vede, ma la scheda resta del suo Tutor (403);
     * le altre sono come inesistenti (404).
     */
    private Ticket ownTicket(String ticketId) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = Optional.ofNullable(TsidMapper.toInternal(ticketId))
                .flatMap(ticketRepository::findById)
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
        if (ticket.getTutor().getId().equals(me.getId())) {
            return ticket;
        }
        if (userProjectRepository.existsById(new UserProject.Id(me.getId(), ticket.getProject().getId()))) {
            throw new AccessDeniedException("Solo il Tutor della segnalazione può usare la scheda del tirocinio");
        }
        throw new NotFoundException("Segnalazione non trovata");
    }

    /** Segnalazione del Tutor corrente con la scheda: dall'appuntamento fissato in poi. */
    private Ticket internshipTicket(String ticketId) {
        Ticket ticket = ownTicket(ticketId);
        if (!WITH_INTERNSHIP.contains(ticket.getStatus())) {
            throw new ConflictException("La scheda del tirocinio si compila dopo l'appuntamento fissato");
        }
        return ticket;
    }

    /** Azienda della mansione abbinata; nulla solo se la segnalazione non ne ha più una. */
    private static Company company(Ticket ticket) {
        return ticket.getJobSlot() == null ? null : ticket.getJobSlot().getCompany();
    }

    /** Tipo attivo del catalogo, oppure quello già scelto anche se nel frattempo disattivato. */
    private InternshipType internshipType(String id, InternshipType current) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(internshipTypeRepository::findById)
                .filter(t -> t.isActive() || t.equals(current))
                .orElseThrow(() -> new FieldValidationException("internshipTypeId", "Tipo di tirocinio non valido"));
    }

    private CompanyAtecoCode atecoCode(String id, Company company) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(atecoCodeRepository::findById)
                .filter(a -> company != null && a.getCompany().getId().equals(company.getId()))
                .orElseThrow(() -> new FieldValidationException("atecoCodeId",
                        "Scegli un codice ATECO dell'azienda del tirocinio"));
    }

    /** Sospensioni nell'ordine giusto, dentro il periodo del tirocinio (se noto) e senza sovrapposizioni. */
    private static void checkSuspensions(InternshipFormDTO dto) {
        List<SuspensionDTO> rows = dto.suspensions();
        for (int i = 0; i < rows.size(); i++) {
            SuspensionDTO row = rows.get(i);
            if (row.to().isBefore(row.from())) {
                throw new FieldValidationException("suspensions[%d].to".formatted(i),
                        "La fine non può precedere l'inizio");
            }
            if (dto.startDate() != null && row.from().isBefore(dto.startDate())) {
                throw new FieldValidationException("suspensions[%d].from".formatted(i),
                        "Il periodo inizia prima del tirocinio");
            }
            if (dto.expectedEndDate() != null && row.to().isAfter(dto.expectedEndDate())) {
                throw new FieldValidationException("suspensions[%d].to".formatted(i),
                        "Il periodo finisce dopo la fine prevista del tirocinio");
            }
            for (int j = 0; j < i; j++) {
                SuspensionDTO other = rows.get(j);
                if (!row.from().isAfter(other.to()) && !other.from().isAfter(row.to())) {
                    throw new FieldValidationException("suspensions[%d].from".formatted(i),
                            "Si sovrappone al periodo dal %s al %s".formatted(DAY.format(other.from()),
                                    DAY.format(other.to())));
                }
            }
        }
    }

    /**
     * Griglia oraria (US-1002): fasce in ordine, ogni giorno una sola volta e con almeno una fascia, ore del giorno non
     * oltre le fasce scelte, totale settimanale entro il massimo.
     */
    private static void checkSchedule(InternshipFormDTO dto) {
        if (!dto.firstSlotStart().isBefore(dto.firstSlotEnd())) {
            throw new FieldValidationException("firstSlotEnd", "La fascia deve finire dopo l'inizio");
        }
        if (!dto.secondSlotStart().isBefore(dto.secondSlotEnd())) {
            throw new FieldValidationException("secondSlotEnd", "La fascia deve finire dopo l'inizio");
        }
        if (dto.secondSlotStart().isBefore(dto.firstSlotEnd())) {
            throw new FieldValidationException("secondSlotStart",
                    "La seconda fascia non può iniziare prima della fine della prima");
        }
        InternshipHours.Slots slots = new InternshipHours.Slots(dto.firstSlotStart(), dto.firstSlotEnd(),
                dto.secondSlotStart(), dto.secondSlotEnd());
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < dto.schedule().size(); i++) {
            ScheduleDayDTO day = dto.schedule().get(i);
            if (!seen.add(day.dayOfWeek())) {
                throw new FieldValidationException("schedule", "Ogni giorno può comparire una sola volta");
            }
            if (!day.firstSlot() && !day.secondSlot()) {
                throw new FieldValidationException("schedule[%d]".formatted(i), "Scegli almeno una fascia");
            }
            int max = InternshipHours.slotMinutes(slots, hoursDay(day));
            if (day.minutes() != null && day.minutes() > max) {
                throw new FieldValidationException("schedule[%d].minutes".formatted(i),
                        "Al massimo %s nelle fasce scelte".formatted(hours(max)));
            }
        }
        Integer weekly = InternshipHours.weeklyMinutes(slots, dto.schedule().stream()
                .map(InternshipServiceImpl::hoursDay).toList());
        if (weekly != null && weekly > MAX_WEEKLY_MINUTES) {
            throw new FieldValidationException("schedule",
                    "Il totale settimanale (%s) supera le 40 ore".formatted(hours(weekly)));
        }
    }

    /**
     * Righe figlie sostituite in blocco, solo se cambiate. Prima si cancellano le vecchie con un flush: Hibernate
     * inserisce prima di cancellare e un periodo spostato violerebbe {@code ex_internship_suspension_overlap}, un giorno
     * ripetuto {@code uq_internship_schedule_day}.
     */
    private void replaceRows(Internship internship, List<CompanyTutorDTO> tutors, List<SuspensionDTO> suspensions,
            List<ScheduleDayDTO> schedule) {
        boolean tutorsChanged = !tutors.equals(tutors(internship));
        boolean suspensionsChanged = !suspensions.equals(suspensions(internship));
        boolean scheduleChanged = !schedule.equals(schedule(internship));
        if (!tutorsChanged && !suspensionsChanged && !scheduleChanged) {
            return;
        }
        if (tutorsChanged) {
            internship.getCompanyTutors().clear();
        }
        if (suspensionsChanged) {
            internship.getSuspensions().clear();
        }
        if (scheduleChanged) {
            internship.getScheduleDays().clear();
        }
        if (internship.getId() != null) {
            internshipRepository.flush();
        }
        if (tutorsChanged) {
            tutors.forEach(t -> internship.addCompanyTutor(t.fullName(), t.contact()));
        }
        if (suspensionsChanged) {
            suspensions.forEach(s -> internship.addSuspension(s.from(), s.to()));
        }
        if (scheduleChanged) {
            schedule.forEach(d -> internship.addScheduleDay(d.dayOfWeek(), d.firstSlot(), d.secondSlot(), d.minutes()));
        }
    }

    /** Scheda per il Tutor; versione nulla se non ancora salvata. Totale settimanale e monte ore calcolati qui. */
    private InternshipDTO toDto(Ticket ticket, Internship i) {
        Company company = company(ticket);
        String companyId = company == null ? null : TsidMapper.toExternal(company.getId());
        LegalRepresentativeDTO rep = companyId == null ? null
                : companyLegalDataService.legalRepresentative(companyId).orElse(null);
        List<AtecoCodeDTO> codes = companyId == null ? List.of() : companyLegalDataService.atecoCodes(companyId);
        List<ScheduleDayDTO> schedule = schedule(i);
        InternshipHours.Slots slots = new InternshipHours.Slots(i.getFirstSlotStart(), i.getFirstSlotEnd(),
                i.getSecondSlotStart(), i.getSecondSlotEnd());
        List<InternshipHours.Day> days = schedule.stream().map(InternshipServiceImpl::hoursDay).toList();
        Integer total = InternshipHours.totalMinutes(slots, days, i.getStartDate(), i.getExpectedEndDate(),
                suspensions(i).stream().map(s -> new InternshipHours.Period(s.from(), s.to())).toList(),
                i.isExcludeHolidays());
        return new InternshipDTO(i.getId() == null ? null : i.getVersion(),
                company == null ? null : company.getName(), rep, codes,
                referenceMapper.toDto(i.getInternshipType()), i.getStartDate(), i.getExpectedEndDate(), i.getSite(),
                i.getAtecoCode() == null ? null : TsidMapper.toExternal(i.getAtecoCode().getId()), tutors(i),
                i.getFiscalCode(), i.getBirthDate(), i.getBirthPlace(), i.getResidence(), suspensions(i), i.getNotes(),
                i.getFirstSlotStart(), i.getFirstSlotEnd(), i.getSecondSlotStart(), i.getSecondSlotEnd(),
                i.isExcludeHolidays(), schedule, InternshipHours.weeklyMinutes(slots, days), total);
    }

    private static List<ScheduleDayDTO> schedule(Internship i) {
        return i.getScheduleDays().stream()
                .map(d -> new ScheduleDayDTO(d.getDayOfWeek(), d.isFirstSlot(), d.isSecondSlot(), d.getMinutes()))
                .sorted(Comparator.comparing(ScheduleDayDTO::dayOfWeek))
                .toList();
    }

    private static InternshipHours.Day hoursDay(ScheduleDayDTO d) {
        return new InternshipHours.Day(DayOfWeek.of(d.dayOfWeek()), d.firstSlot(), d.secondSlot(), d.minutes());
    }

    /** Ore in italiano per i messaggi: "1 ora", "4 ore", "2,5 ore". */
    private static String hours(int minutes) {
        if (minutes == 60) {
            return "1 ora";
        }
        String value = minutes % 60 == 0 ? String.valueOf(minutes / 60)
                : String.valueOf(minutes / 60.0).replace('.', ',');
        return value + " ore";
    }

    private static String slotsText(LocalTime firstStart, LocalTime firstEnd, LocalTime secondStart,
            LocalTime secondEnd) {
        return "%s-%s, %s-%s".formatted(firstStart, firstEnd, secondStart, secondEnd);
    }

    /** Griglia per l'audit: giorno (1 = lunedì), fasce e minuti se indicati, es. "2 F1+F2 180'". */
    private static List<String> scheduleText(List<ScheduleDayDTO> schedule) {
        return schedule.stream()
                .map(d -> "%d %s%s".formatted(d.dayOfWeek(),
                        d.firstSlot() && d.secondSlot() ? "F1+F2" : d.firstSlot() ? "F1" : "F2",
                        d.minutes() == null ? "" : " " + d.minutes() + "'"))
                .toList();
    }

    private static List<CompanyTutorDTO> tutors(Internship i) {
        return i.getCompanyTutors().stream().map(t -> new CompanyTutorDTO(t.getFullName(), t.getContact())).toList();
    }

    private static List<SuspensionDTO> suspensions(Internship i) {
        return i.getSuspensions().stream()
                .map(s -> new SuspensionDTO(s.getDateFrom(), s.getDateTo()))
                .sorted(Comparator.comparing(SuspensionDTO::from))
                .toList();
    }

    private static List<String> periods(List<SuspensionDTO> suspensions) {
        return suspensions.stream().map(s -> s.from() + "/" + s.to()).toList();
    }

    private static String code(InternshipType type) {
        return type == null ? null : type.getCode();
    }

    private static String atecoText(CompanyAtecoCode ateco) {
        return ateco == null ? null : ateco.getCode();
    }

    private static String text(LocalDate date) {
        return Objects.toString(date, null);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
