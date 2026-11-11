package it.nexus.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyAtecoCode;
import it.nexus.domain.Internship;
import it.nexus.domain.InternshipType;
import it.nexus.domain.Municipality;
import it.nexus.domain.Project;
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
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.domain.dto.ScheduleDayDTO;
import it.nexus.domain.dto.SuspensionDTO;
import it.nexus.domain.dto.TrainingProjectDTO;
import it.nexus.domain.dto.TrainingProjectFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.workflow.InternshipHours;
import it.nexus.domain.workflow.InternshipMonths;
import it.nexus.mapper.MunicipalityMapper;
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
import it.nexus.services.MunicipalityService;
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
    /** Date dei messaggi sui progetti formativi, come le scrive il modulo (US-1008). */
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
    /** Totale settimanale massimo, segnaposto da confermare con i moduli ufficiali (Q5). */
    private static final int MAX_WEEKLY_MINUTES = 40 * 60;
    /** Dati richiesti per l'avvio (review dello Sprint 7, §8.2) e come si chiamano nel messaggio di rifiuto. */
    private static final Map<String, String> START_LABELS = Map.ofEntries(
            Map.entry("internshipType", "il tipo di tirocinio"),
            Map.entry("startDate", "la data di inizio"),
            Map.entry("expectedEndDate", "la data di fine prevista"),
            Map.entry("site", "la sede con il comune"),
            Map.entry("atecoCode", "il codice ATECO"),
            Map.entry("companyTutors", "almeno un tutor aziendale"),
            Map.entry("schedule", "la griglia oraria"),
            // convenzione e progetti formativi obbligatori per l'avvio (PO, 11/11)
            Map.entry("agreement", "la convenzione (numero, data e data di stipula)"),
            Map.entry("trainingProjects", "almeno un progetto formativo"),
            Map.entry("fiscalCode", "il codice fiscale del beneficiario"),
            Map.entry("birthDate", "la data di nascita del beneficiario"),
            Map.entry("birthPlace", "il luogo di nascita del beneficiario"),
            Map.entry("residence", "la residenza del beneficiario (comune, via e numero civico)"),
            Map.entry("legalRepresentative", "il legale rappresentante dell'azienda"),
            Map.entry("legalRepresentativeData", "codice fiscale, nascita e residenza del legale rappresentante"));

    /** Come chiamare i dati mancanti per l'inclusione nel messaggio d'errore (US-206). */
    private static final Map<String, String> INCLUSION_LABELS = Map.of(
            "careAgency", "l'ente che lo ha in carico",
            "caseManagerName", "il nome del case manager",
            "caseManagerEmail", "la mail del case manager",
            "caseManagerPhone", "il telefono del case manager");

    private final InternshipRepository internshipRepository;
    private final TicketRepository ticketRepository;
    private final UserProjectRepository userProjectRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final CompanyAtecoCodeRepository atecoCodeRepository;
    private final CompanyLegalDataService companyLegalDataService;
    private final CurrentAppUserService currentAppUserService;
    private final AuditService auditService;
    private final ReferenceMapper referenceMapper;
    private final MunicipalityService municipalityService;

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
        InternshipType type = internshipType(dto.internshipTypeId(), internship.getInternshipType(),
                ticket.getProject());
        if (type != null && type.isInclusion() && !type.equals(internship.getInternshipType())) {
            List<String> missing = inclusionMissing(ticket.getBeneficiary());
            if (!missing.isEmpty()) {
                throw new FieldValidationException("internshipTypeId",
                        "Per un tirocinio di inclusione completa nella scheda del beneficiario: "
                                + missing.stream().map(INCLUSION_LABELS::get).collect(Collectors.joining(", ")));
            }
        }
        CompanyAtecoCode ateco = atecoCode(dto.atecoCodeId(), company(ticket));
        Municipality siteMunicipality = municipalityService.forAddress(dto.siteMunicipalityId(),
                internship.getSiteMunicipality(), "siteMunicipalityId");
        Municipality residenceMunicipality = municipalityService.forAddress(dto.residenceMunicipalityId(),
                internship.getResidenceMunicipality(), "residenceMunicipalityId");
        String residenceStreet = blankToNull(dto.residenceStreet());
        String residenceStreetNumber = blankToNull(dto.residenceStreetNumber());
        if (dto.startDate() != null && dto.expectedEndDate() != null
                && dto.expectedEndDate().isBefore(dto.startDate())) {
            throw new FieldValidationException("expectedEndDate", "La fine prevista non può precedere l'inizio");
        }
        checkSuspensions(dto);
        checkTrainingProjects(dto);
        checkSchedule(dto);
        String fiscalCode = blankToNull(dto.fiscalCode());
        fiscalCode = fiscalCode == null ? null : fiscalCode.toUpperCase(Locale.ROOT);
        if (fiscalCode != null && dto.birthDate() != null && !FiscalCodes.matchesBirthDate(fiscalCode, dto.birthDate())) {
            throw new FieldValidationException("fiscalCode", "Il codice fiscale non corrisponde alla data di nascita");
        }
        List<CompanyTutorDTO> tutors = dto.companyTutors().stream()
                .map(t -> new CompanyTutorDTO(t.fullName().strip(), blankToNull(t.role()),
                        blankToNull(t.contact())))
                .toList();
        List<SuspensionDTO> suspensions = dto.suspensions().stream()
                .sorted(Comparator.comparing(SuspensionDTO::from))
                .toList();
        List<ScheduleDayDTO> schedule = dto.schedule().stream()
                .sorted(Comparator.comparing(ScheduleDayDTO::dayOfWeek))
                .toList();
        // già nell'ordine delle date (controllato): la posizione è il numero
        List<TrainingProjectFormDTO> projects = dto.trainingProjects().stream()
                .map(p -> new TrainingProjectFormDTO(p.from(), p.to(), p.jobTitle().strip(), blankToNull(p.jobDetails())))
                .toList();
        String agreementNumber = blankToNull(dto.agreementNumber());

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
                .value("siteMunicipality", MunicipalityMapper.label(internship.getSiteMunicipality()),
                        MunicipalityMapper.label(siteMunicipality))
                .value("atecoCode", atecoText(internship.getAtecoCode()), atecoText(ateco))
                .value("suspensions", periods(suspensions(internship)), periods(suspensions))
                // convenzione e progetti formativi con i valori, il dettaglio della mansione solo come "cambiato" (US-1008)
                .value("agreementNumber", internship.getAgreementNumber(), agreementNumber)
                .value("agreementDate", text(internship.getAgreementDate()), text(dto.agreementDate()))
                .value("agreementSignedDate", text(internship.getAgreementSignedDate()), text(dto.agreementSignedDate()))
                .value("trainingProjects", projectsText(trainingProjects(internship)), projectsText(projects))
                .redactedIfChanged("trainingProjectDetails",
                        trainingProjects(internship).stream().map(TrainingProjectFormDTO::jobDetails).toList(),
                        projects.stream().map(TrainingProjectFormDTO::jobDetails).toList())
                .redactedIfChanged("companyTutors", tutors(internship), tutors)
                .redactedIfChanged("fiscalCode", internship.getFiscalCode(), fiscalCode)
                .redactedIfChanged("birthDate", internship.getBirthDate(), dto.birthDate())
                .redactedIfChanged("birthPlace", internship.getBirthPlace(), blankToNull(dto.birthPlace()))
                // la residenza è una voce sola, cambiata se cambia una delle sue parti (US-1007)
                .redactedIfChanged("residence", residence(internship.getResidenceMunicipality(),
                        internship.getResidenceStreet(), internship.getResidenceStreetNumber()),
                        residence(residenceMunicipality, residenceStreet, residenceStreetNumber))
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
        internship.setSiteMunicipality(siteMunicipality);
        internship.setAtecoCode(ateco);
        internship.setFiscalCode(fiscalCode);
        internship.setBirthDate(dto.birthDate());
        internship.setBirthPlace(blankToNull(dto.birthPlace()));
        internship.setResidenceMunicipality(residenceMunicipality);
        internship.setResidenceStreet(residenceStreet);
        internship.setResidenceStreetNumber(residenceStreetNumber);
        internship.setNotes(blankToNull(dto.notes()));
        internship.setAgreementNumber(agreementNumber);
        internship.setAgreementDate(dto.agreementDate());
        internship.setAgreementSignedDate(dto.agreementSignedDate());
        replaceRows(internship, tutors, suspensions, schedule, projects);
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
    public void requireCompleteForStart(Ticket ticket) {
        Internship internship = internshipRepository.findByTicketId(ticket.getId())
                .orElseGet(() -> new Internship(ticket));
        List<String> missing = missingForStart(internship, legalRepresentative(company(ticket)));
        if (!missing.isEmpty()) {
            throw new ConflictException("Per avviare il tirocinio la scheda deve essere completa; mancano: %s",
                    missing.stream().map(START_LABELS::get).collect(Collectors.joining(", ")));
        }
    }

    @Override
    public boolean clearCompanyData(Ticket ticket) {
        return internshipRepository.findByTicketId(ticket.getId())
                .filter(i -> i.getInternshipType() != null || i.getStartDate() != null || i.getExpectedEndDate() != null
                        || i.getSite() != null || i.getSiteMunicipality() != null || i.getAtecoCode() != null
                        || i.getNotes() != null
                        || !i.getCompanyTutors().isEmpty() || !i.getSuspensions().isEmpty()
                        || !i.getScheduleDays().isEmpty()
                        // convenzione e progetti formativi (US-1008)
                        || i.getAgreementNumber() != null || i.getAgreementDate() != null
                        || i.getAgreementSignedDate() != null || !i.getTrainingProjects().isEmpty())
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

    /**
     * Tipo attivo del catalogo ammesso dal progetto (US-115), oppure quello già scelto anche se nel frattempo
     * disattivato o non più ammesso.
     */
    private InternshipType internshipType(String id, InternshipType current, Project project) {
        if (id == null || id.isBlank()) {
            return null;
        }
        InternshipType type = Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(internshipTypeRepository::findById)
                .filter(t -> t.isActive() || t.equals(current))
                .orElseThrow(() -> new FieldValidationException("internshipTypeId", "Tipo di tirocinio non valido"));
        if (!type.equals(current) && !allowed(project, type)) {
            throw new FieldValidationException("internshipTypeId",
                    "Il tipo «%s» non è ammesso dal progetto %s".formatted(type.getName(), project.getName()));
        }
        return type;
    }

    /** Cosa manca al beneficiario per un tirocinio di inclusione (US-206), nell'ordine del modulo. */
    private static List<String> inclusionMissing(Beneficiary b) {
        List<String> missing = new ArrayList<>();
        if (b.getCareAgency() == null) {
            missing.add("careAgency");
        }
        if (b.getCaseManagerName() == null) {
            missing.add("caseManagerName");
        }
        if (b.getCaseManagerEmail() == null) {
            missing.add("caseManagerEmail");
        }
        if (b.getCaseManagerPhone() == null) {
            missing.add("caseManagerPhone");
        }
        return missing;
    }

    /** Un progetto senza tipi indicati li ammette tutti (fino al censimento dei progetti). */
    private static boolean allowed(Project project, InternshipType type) {
        return project.getAllowedInternshipTypes().isEmpty() || project.getAllowedInternshipTypes().contains(type);
    }

    /** I tipi che il Tutor può scegliere nella scheda: attivi e ammessi dal progetto, più quello già scelto. */
    private List<ReferenceItemDTO> choosableTypes(Project project, InternshipType current) {
        List<InternshipType> types = new ArrayList<>(internshipTypeRepository.findByActiveTrueOrderByNameAsc().stream()
                .filter(t -> allowed(project, t))
                .toList());
        if (current != null && !types.contains(current)) {
            types.add(current);
        }
        return types.stream().map(referenceMapper::toDto).toList();
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
    /**
     * Progetti formativi (US-1008): servono le date del tirocinio; ognuno sta dentro di esse e inizia dopo la fine del
     * precedente, quindi l'elenco è nell'ordine delle date e senza sovrapposizioni, neanche di un giorno.
     */
    private static void checkTrainingProjects(InternshipFormDTO dto) {
        List<TrainingProjectFormDTO> rows = dto.trainingProjects();
        if (rows.isEmpty()) {
            return;
        }
        if (dto.startDate() == null) {
            throw new FieldValidationException("startDate",
                    "Indica la data di inizio: i progetti formativi stanno dentro le date del tirocinio");
        }
        if (dto.expectedEndDate() == null) {
            throw new FieldValidationException("expectedEndDate",
                    "Indica la fine prevista: i progetti formativi stanno dentro le date del tirocinio");
        }
        for (int i = 0; i < rows.size(); i++) {
            TrainingProjectFormDTO row = rows.get(i);
            if (row.to().isBefore(row.from())) {
                throw new FieldValidationException("trainingProjects[%d].to".formatted(i),
                        "La fine non può precedere l'inizio");
            }
            if (row.from().isBefore(dto.startDate())) {
                throw new FieldValidationException("trainingProjects[%d].from".formatted(i),
                        "Il progetto inizia prima del tirocinio (%s)".formatted(DATE.format(dto.startDate())));
            }
            if (row.to().isAfter(dto.expectedEndDate())) {
                throw new FieldValidationException("trainingProjects[%d].to".formatted(i),
                        "Il progetto finisce dopo la fine prevista del tirocinio (%s)"
                                .formatted(DATE.format(dto.expectedEndDate())));
            }
            if (i > 0 && !row.from().isAfter(rows.get(i - 1).to())) {
                throw new FieldValidationException("trainingProjects[%d].from".formatted(i),
                        "Deve iniziare dopo la fine del progetto formativo %d (%s)"
                                .formatted(i, DATE.format(rows.get(i - 1).to())));
            }
        }
    }

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
     * inserisce prima di cancellare e un periodo spostato violerebbe {@code ex_internship_suspension_overlap} (o
     * {@code ex_internship_training_project_overlap}), un giorno ripetuto {@code uq_internship_schedule_day}.
     */
    private void replaceRows(Internship internship, List<CompanyTutorDTO> tutors, List<SuspensionDTO> suspensions,
            List<ScheduleDayDTO> schedule, List<TrainingProjectFormDTO> projects) {
        boolean tutorsChanged = !tutors.equals(tutors(internship));
        boolean suspensionsChanged = !suspensions.equals(suspensions(internship));
        boolean scheduleChanged = !schedule.equals(schedule(internship));
        boolean projectsChanged = !projects.equals(trainingProjects(internship));
        if (!tutorsChanged && !suspensionsChanged && !scheduleChanged && !projectsChanged) {
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
        if (projectsChanged) {
            internship.getTrainingProjects().clear();
        }
        if (internship.getId() != null) {
            internshipRepository.flush();
        }
        if (tutorsChanged) {
            tutors.forEach(t -> internship.addCompanyTutor(t.fullName(), t.role(), t.contact()));
        }
        if (suspensionsChanged) {
            suspensions.forEach(s -> internship.addSuspension(s.from(), s.to()));
        }
        if (scheduleChanged) {
            schedule.forEach(d -> internship.addScheduleDay(d.dayOfWeek(), d.firstSlot(), d.secondSlot(), d.minutes()));
        }
        if (projectsChanged) {
            projects.forEach(p -> internship.addTrainingProject(p.from(), p.to(), p.jobTitle(), p.jobDetails()));
        }
    }

    /** Scheda per il Tutor; versione nulla se non ancora salvata. Totale settimanale e monte ore calcolati qui. */
    private InternshipDTO toDto(Ticket ticket, Internship i) {
        Company company = company(ticket);
        String companyId = company == null ? null : TsidMapper.toExternal(company.getId());
        LegalRepresentativeDTO rep = legalRepresentative(company);
        List<AtecoCodeDTO> codes = companyId == null ? List.of() : companyLegalDataService.atecoCodes(companyId);
        List<ScheduleDayDTO> schedule = schedule(i);
        InternshipHours.Slots slots = new InternshipHours.Slots(i.getFirstSlotStart(), i.getFirstSlotEnd(),
                i.getSecondSlotStart(), i.getSecondSlotEnd());
        List<InternshipHours.Day> days = schedule.stream().map(InternshipServiceImpl::hoursDay).toList();
        List<InternshipHours.Period> periods = suspensions(i).stream()
                .map(s -> new InternshipHours.Period(s.from(), s.to()))
                .toList();
        Integer total = InternshipHours.totalMinutes(slots, days, i.getStartDate(), i.getExpectedEndDate(), periods,
                i.isExcludeHolidays());
        // progetti formativi (US-1008): numero, mesi arrotondati e monte ore sulle loro date
        List<TrainingProjectFormDTO> rows = trainingProjects(i);
        List<TrainingProjectDTO> projects = IntStream.range(0, rows.size())
                .mapToObj(k -> {
                    TrainingProjectFormDTO p = rows.get(k);
                    return new TrainingProjectDTO(k + 1, p.from(), p.to(), p.jobTitle(), p.jobDetails(),
                            InternshipMonths.rounded(p.from(), p.to()),
                            InternshipHours.totalMinutes(slots, days, p.from(), p.to(), periods, i.isExcludeHolidays()));
                })
                .toList();
        return new InternshipDTO(i.getId() == null ? null : i.getVersion(),
                company == null ? null : company.getName(), rep, codes,
                referenceMapper.toDto(i.getInternshipType()),
                choosableTypes(ticket.getProject(), i.getInternshipType()), i.getStartDate(), i.getExpectedEndDate(),
                i.getSite(), MunicipalityMapper.toDto(i.getSiteMunicipality()),
                i.getAtecoCode() == null ? null : TsidMapper.toExternal(i.getAtecoCode().getId()), tutors(i),
                i.getFiscalCode(), i.getBirthDate(), i.getBirthPlace(),
                MunicipalityMapper.toDto(i.getResidenceMunicipality()), i.getResidenceStreet(),
                i.getResidenceStreetNumber(), suspensions(i), i.getNotes(),
                ticket.getJobSlot() == null ? null : ticket.getJobSlot().getTitle(),
                i.getAgreementNumber(), i.getAgreementDate(), i.getAgreementSignedDate(), projects,
                i.getFirstSlotStart(), i.getFirstSlotEnd(), i.getSecondSlotStart(), i.getSecondSlotEnd(),
                i.isExcludeHolidays(), schedule, InternshipHours.weeklyMinutes(slots, days), total,
                missingForStart(i, rep), inclusionMissing(ticket.getBeneficiary()));
    }

    private LegalRepresentativeDTO legalRepresentative(Company company) {
        return company == null ? null
                : companyLegalDataService.legalRepresentative(TsidMapper.toExternal(company.getId())).orElse(null);
    }

    /**
     * Dati che mancano per l'avvio, nell'ordine della pagina. Il legale rappresentante può mancare del tutto (lo
     * inserisce il Tutor) o essere incompleto (lo completa il Call Center).
     */
    private static List<String> missingForStart(Internship i, LegalRepresentativeDTO rep) {
        List<String> missing = new ArrayList<>();
        if (i.getInternshipType() == null) {
            missing.add("internshipType");
        }
        if (i.getStartDate() == null) {
            missing.add("startDate");
        }
        if (i.getExpectedEndDate() == null) {
            missing.add("expectedEndDate");
        }
        // sede e residenza complete solo con il comune (US-1007)
        if (i.getSite() == null || i.getSiteMunicipality() == null) {
            missing.add("site");
        }
        if (i.getAtecoCode() == null) {
            missing.add("atecoCode");
        }
        if (i.getCompanyTutors().isEmpty()) {
            missing.add("companyTutors");
        }
        if (i.getScheduleDays().isEmpty()) {
            missing.add("schedule");
        }
        if (i.getAgreementNumber() == null || i.getAgreementDate() == null || i.getAgreementSignedDate() == null) {
            missing.add("agreement");
        }
        if (i.getTrainingProjects().isEmpty()) {
            missing.add("trainingProjects");
        }
        if (i.getFiscalCode() == null) {
            missing.add("fiscalCode");
        }
        if (i.getBirthDate() == null) {
            missing.add("birthDate");
        }
        if (i.getBirthPlace() == null) {
            missing.add("birthPlace");
        }
        if (i.getResidenceMunicipality() == null || i.getResidenceStreet() == null
                || i.getResidenceStreetNumber() == null) {
            missing.add("residence");
        }
        if (rep == null) {
            missing.add("legalRepresentative");
        } else if (rep.fiscalCode() == null || rep.birthDate() == null || rep.birthPlace() == null
                || rep.residence() == null) {
            missing.add("legalRepresentativeData");
        }
        return missing;
    }

    /** Le parti della residenza da confrontare per l'audit: il comune per id, non per istanza. */
    private static List<Object> residence(Municipality municipality, String street, String number) {
        return Arrays.asList(municipality == null ? null : municipality.getId(), street, number);
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
        return i.getCompanyTutors().stream()
                .map(t -> new CompanyTutorDTO(t.getFullName(), t.getRole(), t.getContact()))
                .toList();
    }

    private static List<SuspensionDTO> suspensions(Internship i) {
        return i.getSuspensions().stream()
                .map(s -> new SuspensionDTO(s.getDateFrom(), s.getDateTo()))
                .sorted(Comparator.comparing(SuspensionDTO::from))
                .toList();
    }

    /** Progetti formativi salvati nell'ordine delle date, nella forma del modulo per i confronti. */
    private static List<TrainingProjectFormDTO> trainingProjects(Internship i) {
        return i.getTrainingProjects().stream()
                .map(p -> new TrainingProjectFormDTO(p.getDateFrom(), p.getDateTo(), p.getJobTitle(), p.getJobDetails()))
                .sorted(Comparator.comparing(TrainingProjectFormDTO::from))
                .toList();
    }

    /** Progetti per l'audit: numero, date e mansione, es. "1 2026-12-01/2027-02-28 Aiuto cuoco". */
    private static List<String> projectsText(List<TrainingProjectFormDTO> projects) {
        return IntStream.range(0, projects.size())
                .mapToObj(k -> "%d %s/%s %s".formatted(k + 1, projects.get(k).from(), projects.get(k).to(),
                        projects.get(k).jobTitle()))
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
