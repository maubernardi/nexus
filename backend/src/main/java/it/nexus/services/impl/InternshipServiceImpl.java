package it.nexus.services.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumSet;
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
import it.nexus.domain.dto.SuspensionDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.TicketStatus;
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
        return toDto(ticket, internshipRepository.findByTicketId(ticket.getId()).orElse(null));
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

        // dati del tirocinio con i valori; dati personali (beneficiario, tutor aziendali, note) solo come "cambiato"
        AuditChanges changes = AuditChanges.of()
                .value("internshipType", code(internship.getInternshipType()), code(type))
                .value("startDate", text(internship.getStartDate()), text(dto.startDate()))
                .value("expectedEndDate", text(internship.getExpectedEndDate()), text(dto.expectedEndDate()))
                .value("weeklyHours", internship.getWeeklyHours(), dto.weeklyHours())
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
        internship.setWeeklyHours(dto.weeklyHours());
        internship.setSite(blankToNull(dto.site()));
        internship.setAtecoCode(ateco);
        internship.setFiscalCode(fiscalCode);
        internship.setBirthDate(dto.birthDate());
        internship.setBirthPlace(blankToNull(dto.birthPlace()));
        internship.setResidence(blankToNull(dto.residence()));
        internship.setNotes(blankToNull(dto.notes()));
        replaceRows(internship, tutors, suspensions);
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
                        || i.getWeeklyHours() != null || i.getSite() != null || i.getAtecoCode() != null
                        || i.getNotes() != null || !i.getCompanyTutors().isEmpty() || !i.getSuspensions().isEmpty())
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
     * Righe figlie sostituite in blocco, solo se cambiate. Prima si cancellano le vecchie con un flush: Hibernate
     * inserisce prima di cancellare e un periodo spostato violerebbe {@code ex_internship_suspension_overlap}.
     */
    private void replaceRows(Internship internship, List<CompanyTutorDTO> tutors, List<SuspensionDTO> suspensions) {
        boolean tutorsChanged = !tutors.equals(tutors(internship));
        boolean suspensionsChanged = !suspensions.equals(suspensions(internship));
        if (!tutorsChanged && !suspensionsChanged) {
            return;
        }
        if (tutorsChanged) {
            internship.getCompanyTutors().clear();
        }
        if (suspensionsChanged) {
            internship.getSuspensions().clear();
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
    }

    private InternshipDTO toDto(Ticket ticket, Internship i) {
        Company company = company(ticket);
        String companyId = company == null ? null : TsidMapper.toExternal(company.getId());
        LegalRepresentativeDTO rep = companyId == null ? null
                : companyLegalDataService.legalRepresentative(companyId).orElse(null);
        List<AtecoCodeDTO> codes = companyId == null ? List.of() : companyLegalDataService.atecoCodes(companyId);
        String companyName = company == null ? null : company.getName();
        if (i == null) {
            return new InternshipDTO(null, companyName, rep, codes, null, null, null, null, null, null,
                    List.of(), null, null, null, null, List.of(), null);
        }
        return new InternshipDTO(i.getVersion(), companyName, rep, codes,
                referenceMapper.toDto(i.getInternshipType()), i.getStartDate(), i.getExpectedEndDate(),
                i.getWeeklyHours(), i.getSite(),
                i.getAtecoCode() == null ? null : TsidMapper.toExternal(i.getAtecoCode().getId()), tutors(i),
                i.getFiscalCode(), i.getBirthDate(), i.getBirthPlace(), i.getResidence(), suspensions(i), i.getNotes());
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
