package it.nexus.services.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.TimeConfig;
import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.SecurityUtils;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.BoardPost;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyAuditEvent;
import it.nexus.domain.CompetenceSite;
import it.nexus.domain.InternshipType;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.ReferenceCodes;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketCompanyBlacklist;
import it.nexus.domain.TicketStatusHistory;
import it.nexus.domain.UserProject;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.AppointmentFailedDTO;
import it.nexus.domain.dto.AppointmentSetDTO;
import it.nexus.domain.dto.ApproveCategoryDTO;
import it.nexus.domain.dto.ClosureReportDTO;
import it.nexus.domain.dto.ClosureReportFormDTO;
import it.nexus.domain.dto.CompanyExclusionDTO;
import it.nexus.domain.dto.CompanyExclusionFormDTO;
import it.nexus.domain.dto.CompetenceSiteChangeDTO;
import it.nexus.domain.dto.FastTrackRejectDTO;
import it.nexus.domain.dto.InternshipTypeChangeDTO;
import it.nexus.domain.dto.JobSlotAssignmentDTO;
import it.nexus.domain.dto.JobSlotMatchDTO;
import it.nexus.domain.dto.MatchRequestDTO;
import it.nexus.domain.dto.ProposalRejectDTO;
import it.nexus.domain.dto.RejectCategoryDTO;
import it.nexus.domain.dto.InternshipToCloseDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.dto.TicketDetailDTO;
import it.nexus.domain.dto.TicketRequestCorrectionDTO;
import it.nexus.domain.dto.TicketEventDTO;
import it.nexus.domain.dto.TicketListItemDTO;
import it.nexus.domain.dto.TicketSpecialCreateDTO;
import it.nexus.domain.enumeration.AppointmentFailureCause;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.CompanyEventType;
import it.nexus.domain.enumeration.InternshipDocumentKind;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketListView;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;
import it.nexus.domain.workflow.TicketTransitions;
import it.nexus.mapper.BeneficiaryMapper;
import it.nexus.mapper.JobSlotMapper;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TicketMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.CompetenceSiteRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketCompanyBlacklistRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketSpecifications;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.BoardPostService;
import it.nexus.services.ClosureReportService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.FastTrackService;
import it.nexus.services.InternshipEndReminderService;
import it.nexus.services.InternshipService;
import it.nexus.services.InternshipTypeRules;
import it.nexus.services.JobSlotAssignments;
import it.nexus.services.NotificationService;
import it.nexus.services.TicketInternshipTypeService;
import it.nexus.services.TicketService;
import it.nexus.services.TicketStateMachine;
import it.nexus.services.UserDisplayNameService;
import it.nexus.services.WorkingDayService;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    /** Durata del timer della proposta accettata in giorni lavorativi (brief, fase 5). */
    static final int TIMER_WORKING_DAYS = 7;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Stati della coda: nuove segnalazioni e speciali non ancora prese in carico. */

    private final TicketRepository ticketRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final CurrentAppUserService currentAppUserService;
    private final TicketStateMachine stateMachine;
    private final TicketMapper mapper;
    private final AuditEventRepository auditEventRepository;
    private final TicketStatusHistoryRepository historyRepository;
    private final AppUserRepository appUserRepository;
    private final UserDisplayNameService userDisplayNameService;
    private final WorkingDayService workingDayService;
    private final NotificationService notificationService;
    private final Clock clock;
    private final JobSlotRepository jobSlotRepository;
    private final JobSlotAssignments jobSlotAssignments;
    private final TicketCompanyBlacklistRepository blacklistRepository;
    private final CompanyRepository companyRepository;
    private final CompanyAuditEventRepository companyAuditEvents;
    private final AuditService auditService;
    private final BoardPostService boardPostService;
    private final FastTrackService fastTrackService;
    private final InternshipService internshipService;
    private final ClosureReportService closureReportService;
    private final BoardPostRepository boardPostRepository;
    private final JobSlotMapper jobSlotMapper;
    private final BeneficiaryMapper beneficiaryMapper;
    private final ReferenceMapper referenceMapper;
    private final CompetenceSiteRepository competenceSiteRepository;
    private final InternshipTypeRules internshipTypeRules;
    private final TicketInternshipTypeService ticketInternshipTypeService;
    private final InternshipRepository internshipRepository;
    private final InternshipEndReminderService internshipEndReminderService;

    @Override
    @Transactional(readOnly = true)
    public List<InternshipToCloseDTO> internshipsToClose() {
        return internshipEndReminderService.toClose(currentAppUserService.getCurrentAppUser());
    }

    /**
     * Nessuna segnalazione nuova finché il Tutor ha tirocini finiti alla data prevista e non chiusi (US-1203, PO
     * 22/11): li chiude con il report di restituzione o li proroga.
     */
    private void requireInternshipsClosed(AppUser me) {
        List<InternshipToCloseDTO> open = internshipEndReminderService.toClose(me);
        if (!open.isEmpty()) {
            throw new ConflictException("Non puoi aprire nuove segnalazioni finché non chiudi o proroghi i tirocini "
                    + "finiti: %s", open.stream().map(i -> "segnalazione n. " + i.ticketNumber())
                            .collect(Collectors.joining(", ")));
        }
    }

    @Override
    public TicketDTO submit(TicketCreateDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        requireInternshipsClosed(me);
        Beneficiary beneficiary = ownBeneficiary(dto.beneficiaryId(), me);
        ticketRepository.findFirstByBeneficiaryIdAndStatusNot(beneficiary.getId(), TicketStatus.CLOSED).ifPresent(open -> {
            throw new FieldValidationException("beneficiaryId",
                    "Il beneficiario ha già una segnalazione aperta (n. %d)".formatted(open.getNumber()));
        });
        Project project = assignedProject(dto.projectId(), me);
        Ticket ticket = new Ticket(me, project, beneficiary, TicketType.NORMAL);
        ticket.setCompetenceSite(competenceSite(project, dto.competenceSiteId()));
        ticket.setInternshipType(internshipType(dto.internshipTypeId(), project, beneficiary));
        // esattamente una delle due è garantito da @JobChoice sul DTO
        if (!isBlank(dto.jobCategoryId())) {
            ticket.setRequestedJobCategory(activeJobCategory(dto.jobCategoryId()));
            return mapper.toDto(stateMachine.create(ticket, TicketTransitions.SUBMIT, null));
        }
        // nuova tipologia (US-302): se esiste già nel catalogo va scelta dall'elenco
        String text = dto.jobFreeText().strip();
        jobCategoryRepository.findFirstByNameIgnoreCaseAndActiveTrue(text).ifPresent(existing -> {
            throw new FieldValidationException("jobFreeText",
                    "La tipologia «%s» è già nell'elenco: selezionala".formatted(existing.getName()));
        });
        ticket.setRequestedJobFreeText(text);
        return mapper.toDto(stateMachine.create(ticket, TicketTransitions.SUBMIT_NEW_CATEGORY, null));
    }

    @Override
    public TicketDTO submitSpecial(TicketSpecialCreateDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        requireInternshipsClosed(me);
        BoardPost post = visibleBoardPost(dto.boardPostId(), me);
        if (post.getStatus() == BoardPostStatus.ARCHIVED) {
            throw new ConflictException("L'annuncio #%d non è più in bacheca", post.getNumber());
        }
        Beneficiary beneficiary = ownBeneficiary(dto.beneficiaryId(), me);
        ticketRepository.findFirstByBeneficiaryIdAndStatusNot(beneficiary.getId(), TicketStatus.CLOSED).ifPresent(open -> {
            throw new FieldValidationException("beneficiaryId",
                    "Il beneficiario ha già una segnalazione aperta (n. %d)".formatted(open.getNumber()));
        });
        // una sola candidatura per annuncio: la prima lo blocca finché il Call Center non decide (review Sprint 6, §8.1)
        ticketRepository.findFirstByBoardPostIdAndFastTrackTrue(post.getId()).ifPresent(pending -> {
            throw new ConflictException("L'annuncio #%d ha già una candidatura in valutazione", post.getNumber());
        });
        // annuncio riservato: vale il suo progetto; aperto a tutti: uno dei progetti del Tutor
        Project project = post.getProject() != null ? post.getProject() : assignedProject(dto.projectId(), me);
        Ticket ticket = new Ticket(me, project, beneficiary, TicketType.SPECIAL);
        ticket.setCompetenceSite(competenceSite(project, dto.competenceSiteId()));
        ticket.setInternshipType(internshipType(dto.internshipTypeId(), project, beneficiary));
        ticket.setFastTrack(true);
        ticket.setBoardPost(post);
        ticket.setRequestedJobCategory(post.getJobSlot().getJobCategory());
        return mapper.toDto(stateMachine.create(ticket, TicketTransitions.SUBMIT_SPECIAL, null));
    }

    @Override
    public TicketDetailDTO changeInternshipType(String ticketId, InternshipTypeChangeDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "indicare il tipo di tirocinio");
        InternshipType type = internshipTypeRules.choose(dto.internshipTypeId(), ticket.getInternshipType(),
                ticket.getProject(), ticket.getBeneficiary());
        if (ticketInternshipTypeService.change(ticket, type, false)) {
            // la scheda ha lo stesso tipo della segnalazione
            internshipRepository.findByTicketId(ticket.getId()).ifPresent(i -> i.setInternshipType(type));
        }
        return detail(ticket, me);
    }

    @Override
    public TicketDetailDTO correctRequest(String ticketId, TicketRequestCorrectionDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "correggere progetto e mansione");
        if (!canCorrectRequest(ticket, me)) {
            throw new ConflictException(ticket.getType() == TicketType.SPECIAL
                    ? "La candidatura dalla bacheca ha la mansione dell'annuncio: progetto e mansione non si correggono"
                    : "Dopo la proposta all'azienda progetto e mansione non si correggono più, finché la "
                            + "segnalazione non è riaperta");
        }
        if (ticket.getVersion() != dto.version()) {
            throw new ConflictException("La segnalazione n. %d è cambiata nel frattempo: ricarica la pagina",
                    ticket.getNumber());
        }
        AuditChanges changes = AuditChanges.of();
        boolean changed = false;
        Project projectBefore = ticket.getProject();
        if (!canCorrectProject(ticket)) {
            // riaperta: si corregge la mansione per le nuove proposte, il progetto resta (PO, 28/11)
            if (!projectBefore.getId().equals(TsidMapper.toInternal(dto.projectId().strip()))) {
                throw new FieldValidationException("projectId",
                        "Nella segnalazione riaperta si corregge solo la mansione: il progetto resta %s"
                                .formatted(projectBefore.getName()));
            }
        } else {
            Project project = assignedProject(dto.projectId(), me);
            if (!project.equals(projectBefore)) {
                // con il progetto nuovo, sede e tipo devono valere per lui (US-308, US-605)
                CompetenceSite siteBefore = ticket.getCompetenceSite();
                CompetenceSite site = competenceSite(project, dto.competenceSiteId());
                InternshipType typeBefore = ticket.getInternshipType();
                InternshipType type = internshipTypeRules.choose(dto.internshipTypeId(), typeBefore, project,
                        ticket.getBeneficiary());
                if (type == null) {
                    throw new FieldValidationException(InternshipTypeRules.FIELD, "Scegli il tipo di tirocinio");
                }
                if (!InternshipTypeRules.allowedFor(project, type)) {
                    throw new FieldValidationException(InternshipTypeRules.FIELD,
                            "Il tipo «%s» non è ammesso dal progetto %s".formatted(type.getName(),
                                    project.getName()));
                }
                ticket.setProject(project);
                ticket.setCompetenceSite(site);
                ticket.setInternshipType(type);
                changes.value("project", projectBefore.getName(), project.getName());
                if (!Objects.equals(siteBefore, site)) {
                    changes.value("competenceSite", siteBefore == null ? null : siteBefore.getName(),
                            site == null ? null : site.getName());
                }
                if (!type.equals(typeBefore)) {
                    changes.value("internshipType", typeBefore == null ? null : typeBefore.getName(),
                            type.getName());
                    internshipRepository.findByTicketId(ticket.getId()).ifPresent(i -> i.setInternshipType(type));
                }
                changed = true;
            }
        }
        if (ticket.getRequestedJobCategory() != null) {
            if (isBlank(dto.jobCategoryId())) {
                throw new FieldValidationException("jobCategoryId", "Scegli la mansione");
            }
            JobCategory before = ticket.getRequestedJobCategory();
            // quella già scelta resta anche se nel frattempo disattivata; una nuova deve essere attiva
            if (!before.getId().equals(TsidMapper.toInternal(dto.jobCategoryId().strip()))) {
                JobCategory category = activeJobCategory(dto.jobCategoryId().strip());
                ticket.setRequestedJobCategory(category);
                changes.value("requestedJobCategory", before.getName(), category.getName());
                changed = true;
            }
        } else {
            // tipologia nuova in attesa di approvazione (US-302): si corregge il testo
            String text = dto.jobFreeText() == null ? "" : dto.jobFreeText().strip();
            if (text.isEmpty()) {
                throw new FieldValidationException("jobFreeText", "Scrivi la mansione");
            }
            String before = ticket.getRequestedJobFreeText();
            if (!text.equals(before)) {
                ticket.setRequestedJobFreeText(text);
                changes.value("requestedJobFreeText", before, text);
                changed = true;
            }
        }
        if (changed) {
            ticketRepository.saveAndFlush(ticket);
            auditService.record(AuditEntityType.TICKET, ticket.getId(), "REQUEST_CORRECTED", changes, null);
        }
        return detail(ticket, me);
    }

    @Override
    public TicketDetailDTO changeCompetenceSite(String ticketId, CompetenceSiteChangeDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = visibleTicket(ticketId, me);
        if (!canChangeCompetenceSite(ticket, me)) {
            throw new AccessDeniedException(
                    "La sede di competenza la correggono solo l'amministrazione e il Coordinatore del progetto");
        }
        CompetenceSite before = ticket.getCompetenceSite();
        CompetenceSite site = competenceSiteRepository.findActiveForProject(ticket.getProject().getId()).stream()
                .filter(s -> s.getId().equals(TsidMapper.toInternal(dto.competenceSiteId())))
                .findFirst()
                .orElseThrow(() -> new FieldValidationException("competenceSiteId",
                        "Scegli una sede di competenza del progetto %s".formatted(ticket.getProject().getName())));
        if (!site.equals(before)) {
            ticket.setCompetenceSite(site);
            ticketRepository.saveAndFlush(ticket);
            auditService.record(AuditEntityType.TICKET, ticket.getId(), "COMPETENCE_SITE_CHANGED", AuditChanges.of()
                    .value("competenceSite", before == null ? null : before.getName(), site.getName()), null);
        }
        return detail(ticket, me);
    }

    /** Tipo di tirocinio scelto alla creazione (US-605): obbligatorio, con le regole della scheda. */
    private InternshipType internshipType(String id, Project project, Beneficiary beneficiary) {
        InternshipType type = internshipTypeRules.choose(id, null, project, beneficiary);
        if (type == null) {
            throw new FieldValidationException(InternshipTypeRules.FIELD, "Scegli il tipo di tirocinio");
        }
        return type;
    }

    /**
     * Sede di competenza scelta alla creazione (US-308): tra le sedi attive del progetto, obbligatoria se ce ne sono;
     * un progetto senza sedi non ne ha.
     */
    private CompetenceSite competenceSite(Project project, String siteId) {
        List<CompetenceSite> sites = competenceSiteRepository.findActiveForProject(project.getId());
        if (isBlank(siteId)) {
            if (!sites.isEmpty()) {
                throw new FieldValidationException("competenceSiteId", "Scegli la sede di competenza");
            }
            return null;
        }
        Long id = TsidMapper.toInternal(siteId);
        return sites.stream().filter(s -> s.getId().equals(id)).findFirst()
                .orElseThrow(() -> new FieldValidationException("competenceSiteId",
                        "Scegli una sede di competenza del progetto %s".formatted(project.getName())));
    }

    /** ADMIN, oppure Tutor Coordinatore del progetto della segnalazione (US-112). */
    private boolean canChangeCompetenceSite(Ticket ticket, AppUser me) {
        return me.getRole() == Role.ADMIN || me.getRole() == Role.TUTOR && userProjectRepository
                .findById(new UserProject.Id(me.getId(), ticket.getProject().getId()))
                .map(UserProject::isCoordinator)
                .orElse(false);
    }

    @Override
    public TicketDetailDTO approveCategory(String ticketId, ApproveCategoryDTO dto) {
        Ticket ticket = findTicket(ticketId);
        String name = dto.name().strip();
        jobCategoryRepository.findFirstByNameIgnoreCaseAndActiveTrue(name).ifPresent(existing -> {
            throw new FieldValidationException("name", ("La tipologia «%s» è già nel catalogo: "
                    + "respingi la proposta scegliendo quella").formatted(existing.getName()));
        });
        String proposed = ticket.getRequestedJobFreeText();
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.APPROVE_CATEGORY, dto.version(), null, t -> {
            JobCategory category = jobCategoryRepository.save(new JobCategory(newCategoryCode(name), name));
            auditService.record(AuditEntityType.JOB_CATEGORY, category.getId(), "CREATE", AuditChanges.of()
                    .value("code", null, category.getCode())
                    .value("name", null, category.getName()), null);
            t.setRequestedJobFreeText(null);
            t.setRequestedJobCategory(category);
            return AuditChanges.of()
                    .value("requestedJobFreeText", proposed, null)
                    .value("requestedJobCategory", null, category.getName());
        });
        notificationService.notify(saved.getTutor(), NotificationType.CATEGORY_APPROVED, saved);
        return detail(saved, currentAppUserService.getCurrentAppUser());
    }

    @Override
    public TicketDetailDTO rejectCategory(String ticketId, RejectCategoryDTO dto) {
        Ticket ticket = findTicket(ticketId);
        JobCategory chosen = activeJobCategory(dto.jobCategoryId());
        String proposed = ticket.getRequestedJobFreeText();
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.REJECT_CATEGORY, dto.version(), dto.reason().strip(),
                t -> {
                    t.setRequestedJobFreeText(null);
                    t.setRequestedJobCategory(chosen);
                    return AuditChanges.of()
                            .value("requestedJobFreeText", proposed, null)
                            .value("requestedJobCategory", null, chosen.getName());
                });
        notificationService.notify(saved.getTutor(), NotificationType.CATEGORY_CHANGED, saved);
        return detail(saved, currentAppUserService.getCurrentAppUser());
    }

    /** Codice del catalogo dal nome (es. "Tecnico del suono" → TECNICO_DEL_SUONO), reso univoco se serve. */
    private String newCategoryCode(String name) {
        return ReferenceCodes.fromName(name, "TIPOLOGIA", jobCategoryRepository::existsByCode);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public TicketListItemDTO takeCharge(String ticketId, long expectedVersion) {
        Ticket ticket = Optional.ofNullable(TsidMapper.toInternal(ticketId))
                .flatMap(ticketRepository::findById)
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
        AppUser me = currentAppUserService.getCurrentAppUser();
        if (ticket.getStatus() != TicketStatus.CLOSED && ticket.getStatus() != TicketStatus.IN_ATTESA_APPROVAZIONE
                && !TicketTransitions.TAKE_CHARGE.from().contains(ticket.getStatus())) {
            return mapper.toListItem(takeBack(ticket, expectedVersion, me));
        }
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.TAKE_CHARGE, expectedVersion, null, t -> {
            // una speciale in lavorazione può essere già assegnata: la coda mostra solo quelle libere
            if (t.getAssignedCcOperator() != null) {
                throw new ConflictException("La segnalazione n. %d è già stata presa in carico", t.getNumber());
            }
            t.setAssignedCcOperator(me);
            return AuditChanges.of().value("assignedCcOperatorId", null, TsidMapper.toExternal(me.getId()));
        });
        return mapper.toListItem(saved);
    }

    /**
     * Segnalazione più avanti nel percorso, tornata in coda perché l'operatore che l'aveva in carico è uscito (US-105):
     * chi la prende la segue da dove era, senza cambiare stato.
     */
    private Ticket takeBack(Ticket ticket, long expectedVersion, AppUser me) {
        if (ticket.getVersion() != expectedVersion) {
            throw new ConflictException("La segnalazione è stata modificata nel frattempo: ricarica la pagina e riprova");
        }
        if (ticket.getAssignedCcOperator() != null) {
            throw new ConflictException("La segnalazione n. %d è già stata presa in carico", ticket.getNumber());
        }
        ticket.setAssignedCcOperator(me);
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        auditService.record(AuditEntityType.TICKET, saved.getId(), "TAKE_CHARGE",
                AuditChanges.of().value("assignedCcOperatorId", null, TsidMapper.toExternal(me.getId())), null);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketListItemDTO> list(TicketListView requested, TicketStatus status, String projectId) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Set<Role> roles = SecurityUtils.getCurrentUser().map(AuthenticatedUser::roles).orElse(Set.of());
        TicketListView view = requested != null ? requested
                : roles.contains(Role.TUTOR) && !currentAppUserService.hasGlobalVisibility() ? TicketListView.MINE
                : TicketListView.TO_TAKE;
        if (!view.allowedFor(roles)) {
            throw new BadRequestException("Vista non disponibile per il ruolo");
        }
        Long project = filterId(projectId, "progetto");

        Specification<Ticket> spec = TicketSpecifications.fetchListData()
                .and(TicketSpecifications.status(status))
                .and(TicketSpecifications.project(project));
        Sort newestFirst = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("number"));
        Sort oldestFirst = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("number"));
        Sort sort = switch (view) {
            case MINE -> {
                spec = spec.and(TicketSpecifications.tutor(me.getId()));
                yield newestFirst;
            }
            case PROJECT -> {
                // solo i progetti a cui il Tutor è assegnato: mai segnalazioni di altri progetti
                List<Long> mine = userProjectRepository.findByIdUserId(me.getId()).stream()
                        .map(up -> up.getId().getProjectId()).toList();
                spec = spec.and(TicketSpecifications.projectIn(mine));
                yield newestFirst;
            }
            case TO_APPROVE -> {
                spec = spec.and(TicketSpecifications.status(TicketStatus.IN_ATTESA_APPROVAZIONE));
                yield oldestFirst;
            }
            case TO_TAKE -> {
                // libere: nuove, speciali non assegnate e quelle tornate in coda all'uscita dell'operatore (US-105)
                spec = spec.and(TicketSpecifications.open()).and(TicketSpecifications.unassigned())
                        .and(Specification.not(TicketSpecifications.status(TicketStatus.IN_ATTESA_APPROVAZIONE)));
                yield Sort.by(Sort.Order.desc("fastTrack"), Sort.Order.asc("createdAt"), Sort.Order.asc("number"));
            }
            case ASSIGNED_TO_ME -> {
                spec = spec.and(TicketSpecifications.assignedTo(me.getId()));
                if (status == null) {
                    spec = spec.and(TicketSpecifications.open());
                }
                yield oldestFirst;
            }
            case ALL -> newestFirst;
        };
        List<Ticket> found = ticketRepository.findAll(spec, sort);
        if (view == TicketListView.ASSIGNED_TO_ME) {
            // le riaperte in cima: vanno lavorate di nuovo (review dello Sprint 4, §8.2); ordinamento stabile
            found = found.stream()
                    .sorted(Comparator.comparing((Ticket t) -> t.getStatus() != TicketStatus.RIAPERTO))
                    .toList();
        } else if (view == TicketListView.MINE || view == TicketListView.PROJECT) {
            // prima la scadenza più vicina (US-307); le altre restano dalla più recente: ordinamento stabile
            found = found.stream()
                    .sorted(Comparator.comparing(Ticket::getTimerDeadlineAt,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        }
        return found.stream().map(mapper::toListItem).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailDTO getDetail(String ticketId) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        return detail(visibleTicket(ticketId, me), me);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketEventDTO> history(String ticketId) {
        Ticket ticket = visibleTicket(ticketId, currentAppUserService.getCurrentAppUser());
        List<AuditEvent> events = auditEventRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(
                AuditEntityType.TICKET, ticket.getId());
        // segnalazioni nate prima dell'audit (EN-2): i cambi di stato che l'audit non registra vengono dallo storico
        Set<String> inAudit = events.stream()
                .map(e -> status(e, "before") + ">" + status(e, "after"))
                .collect(Collectors.toSet());
        List<TicketStatusHistory> earlier = historyRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()).stream()
                // presa in carico di una speciale (già in lavorazione): lo stato non cambia, nulla da mostrare
                .filter(r -> r.getFromStatus() != r.getToStatus())
                .filter(r -> !inAudit.contains(r.getFromStatus() + ">" + r.getToStatus()))
                .toList();
        Map<String, String> names = userDisplayNameService.displayNames(Stream.concat(
                earlier.stream().map(TicketStatusHistory::getCreatedBy),
                events.stream().map(AuditEvent::getCreatedBy)).toList());
        return Stream.concat(
                earlier.stream().map(r -> new TicketEventDTO(r.getCreatedAt(), names.get(r.getCreatedBy()), "STATUS",
                        r.getFromStatus(), r.getToStatus(), null, r.getNote())),
                events.stream().map(e -> new TicketEventDTO(e.getCreatedAt(), names.get(e.getCreatedBy()), e.getAction(),
                        status(e, "before"), status(e, "after"), describe(e), e.getReason())))
                .toList();
    }

    /** Le correzioni di progetto e mansione (US-309), con prima e dopo: «Progetto: GOL → POLIS; Mansione: …». */
    private String corrections(AuditEvent event) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("project", "Progetto");
        labels.put("requestedJobCategory", "Mansione");
        labels.put("requestedJobFreeText", "Mansione");
        labels.put("competenceSite", "Sede di competenza");
        labels.put("internshipType", "Tipo di tirocinio");
        String text = labels.entrySet().stream()
                .filter(e -> event.getChanges().containsKey(e.getKey()))
                .map(e -> "%s: %s → %s".formatted(e.getValue(),
                        Optional.ofNullable(changed(event, e.getKey(), "before")).orElse("—"),
                        Optional.ofNullable(changed(event, e.getKey(), "after")).orElse("—")))
                .collect(Collectors.joining("; "));
        return text.isEmpty() ? null : text;
    }

    /** Dettaglio leggibile di un evento: chi ha preso in carico, a chi è passata, quale proposta. */
    private String describe(AuditEvent event) {
        return switch (event.getAction()) {
            case "SUBMIT" -> Optional.ofNullable(TsidMapper.toInternal(changed(event, "boardPostId", "after")))
                    .flatMap(boardPostRepository::findById)
                    .map(post -> "Candidatura all'annuncio #%d della bacheca".formatted(post.getNumber()))
                    .orElse(null);
            case "TAKE_CHARGE" -> userName(changed(event, "assignedCcOperatorId", "after"));
            case "TAKE_OVER" -> "%s → %s".formatted(userName(changed(event, "assignedCcOperatorId", "before")),
                    userName(changed(event, "assignedCcOperatorId", "after")));
            case "TRANSFER" -> "%s → %s".formatted(userName(changed(event, "tutorId", "before")),
                    userName(changed(event, "tutorId", "after")));
            case "COMPETENCE_SITE_CHANGED" -> Optional.ofNullable(changed(event, "competenceSite", "before"))
                    .map(before -> before + " → " + changed(event, "competenceSite", "after"))
                    .orElse(changed(event, "competenceSite", "after"));
            case "REQUEST_CORRECTED" -> corrections(event);
            case "INTERNSHIP_TYPE_CHANGED" -> Optional.ofNullable(changed(event, "internshipType", "before"))
                    .map(before -> before + " → " + changed(event, "internshipType", "after"))
                    .orElse(changed(event, "internshipType", "after"));
            case "RELEASE" -> Optional.ofNullable(userName(changed(event, "assignedCcOperatorId", "before")))
                    .map(name -> name + " non è più disponibile: la segnalazione torna libera per un collega")
                    .orElse(null);
            case "MATCH", "FAST_TRACK_APPROVE" -> Optional
                    .ofNullable(TsidMapper.toInternal(changed(event, "jobSlotId", "after")))
                    .flatMap(jobSlotRepository::findById)
                    .map(slot -> "«%s» presso %s".formatted(slot.getTitle(), slot.getCompany().getName()))
                    .orElse(null);
            case "CATEGORY_APPROVE" -> Optional.ofNullable(changed(event, "requestedJobCategory", "after"))
                    .map(name -> "«%s» aggiunta al catalogo".formatted(name))
                    .orElse(null);
            case "CATEGORY_REJECT" -> "«%s» → «%s»".formatted(changed(event, "requestedJobFreeText", "before"),
                    changed(event, "requestedJobCategory", "after"));
            case "APPOINTMENT_SET" -> "Timer fermato";
            case "CLOSE" -> closureReportService.outcome(event.getEntityId()).orElse(null);
            case "DOCUMENT_GENERATED" -> Optional.ofNullable(changed(event, "document", "after"))
                    .map(kind -> documentDetail(InternshipDocumentKind.valueOf(kind).label(),
                            changed(event, "extension", "after"), changed(event, "project", "after")))
                    .orElse(null);
            case "TIMER_REMINDER" -> "Promemoria inviato al Tutor";
            case "TIMER_EXPIRE" -> "Nessun appuntamento entro la scadenza: mansione di nuovo libera";
            case "PROPOSAL_REJECT", "COMPANY_EXCLUDE" -> Optional
                    .ofNullable(TsidMapper.toInternal(changed(event, "excludedCompanyId", "after")))
                    .flatMap(companyRepository::findById)
                    .map(c -> "%s esclusa per questa segnalazione".formatted(c.getName()))
                    .orElse(null);
            case "APPOINTMENT_FAILED" -> Optional.ofNullable(changed(event, "cause", "after"))
                    .map(cause -> "Causa: " + AppointmentFailureCause.valueOf(cause).label()
                            + (changed(event, "boardRelaunch", "after") != null ? "; rilancio in bacheca richiesto" : ""))
                    .orElse(null);
            case "ACCEPT" -> Optional.ofNullable(changed(event, "timerDeadlineAt", "after"))
                    .map(at -> "Scadenza " + DATE.format(TimeConfig.lastDay(Instant.parse(at))))
                    .orElse(null);
            default -> null;
        };
    }

    private String userName(String externalId) {
        return Optional.ofNullable(TsidMapper.toInternal(externalId))
                .flatMap(appUserRepository::findById)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse(null);
    }

    /** Il documento scaricato: «Progetto Formativo 2, proroga 1» (US-1011). */
    private static String documentDetail(String label, String extension, String project) {
        String detail = project == null ? label : label + " " + project;
        return extension == null ? detail : detail + ", proroga " + extension;
    }

    private static String changed(AuditEvent event, String field, String side) {
        Object change = event.getChanges().get(field);
        return change instanceof Map<?, ?> map && map.get(side) != null ? map.get(side).toString() : null;
    }

    private static TicketStatus status(AuditEvent event, String side) {
        String value = changed(event, "status", side);
        return value == null ? null : TicketStatus.fromStored(value);
    }

    /** Ticket visibile all'utente; altrimenti 404, come per un ticket inesistente. */
    private Ticket visibleTicket(String ticketId, AppUser me) {
        Ticket ticket = findTicket(ticketId);
        if (currentAppUserService.hasGlobalVisibility() || ticket.getTutor().getId().equals(me.getId())
                || userProjectRepository.existsById(new UserProject.Id(me.getId(), ticket.getProject().getId()))) {
            return ticket;
        }
        throw new NotFoundException("Segnalazione non trovata");
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobSlotMatchDTO> compatibleJobSlots(String ticketId, String zoneId, String jobCategoryId) {
        Ticket ticket = findTicket(ticketId);
        List<JobSlot> slots = jobSlotRepository.findCompatible(ticket.getId(),
                ticket.getInternshipType() == null ? null : ticket.getInternshipType().getId(),
                filterId(zoneId, "zona"), filterId(jobCategoryId, "tipologia"));
        // le assegnazioni degli altri tipi, per l'avviso (US-605)
        Map<Long, List<JobSlotAssignmentDTO>> assignments =
                jobSlotAssignments.assignments(slots.stream().map(JobSlot::getId).toList());
        return slots.stream()
                .map(s -> jobSlotMapper.toMatch(s).withAssignments(assignments.getOrDefault(s.getId(), List.of())))
                .toList();
    }

    @Override
    public TicketDetailDTO match(String ticketId, MatchRequestDTO request) {
        Ticket ticket = findTicket(ticketId);
        AppUser me = currentAppUserService.getCurrentAppUser();
        JobSlot slot = Optional.ofNullable(TsidMapper.toInternal(request.jobSlotId()))
                .flatMap(jobSlotRepository::findById)
                .orElseThrow(() -> new FieldValidationException("jobSlotId", "Mansione non valida"));

        Ticket saved = stateMachine.apply(ticket, TicketTransitions.MATCH, request.ticketVersion(), null, t -> {
            requireTakenInCharge(t, "abbinarla");
            if (t.isFastTrack()) {
                // prima si decide sulla corsia veloce: approvata è già una proposta, rifiutata torna normale (US-403/404)
                throw new ConflictException("Decidi prima sulla corsia veloce della segnalazione n. %d", t.getNumber());
            }
            if (slot.getVersion() != request.jobSlotVersion()) {
                throw new ConflictException(
                        "La mansione «%s» non è più disponibile: è stata appena bloccata o modificata", slot.getTitle());
            }
            return proposeSlot(t, slot);
        });
        afterProposal(saved, slot, "Mansione abbinata alla segnalazione n. %d".formatted(saved.getNumber()));
        return detail(saved, me);
    }

    @Override
    public TicketDetailDTO approveFastTrack(String ticketId, long expectedVersion) {
        Ticket ticket = findTicket(ticketId);
        AppUser me = currentAppUserService.getCurrentAppUser();
        if (!ticket.isFastTrack() || ticket.getBoardPost() == null) {
            throw new ConflictException("La segnalazione n. %d non ha una corsia veloce in attesa", ticket.getNumber());
        }
        BoardPost post = ticket.getBoardPost();
        JobSlot slot = post.getJobSlot();

        Ticket saved = stateMachine.apply(ticket, TicketTransitions.FAST_TRACK_APPROVE, expectedVersion, null, t -> {
            requireTakenInCharge(t, "decidere sulla corsia veloce");
            if (post.getStatus() != BoardPostStatus.PUBLISHED) {
                throw new ConflictException("L'annuncio #%d non è più in bacheca", post.getNumber());
            }
            AuditChanges changes = proposeSlot(t, slot);
            // la corsia veloce è consumata; il tipo SPECIAL resta come traccia dell'origine dalla bacheca
            t.setFastTrack(false);
            return changes.value("fastTrack", true, false);
        });
        afterProposal(saved, slot, "Corsia veloce approvata: segnalazione n. %d".formatted(saved.getNumber()));
        return detail(saved, me);
    }

    @Override
    public TicketDetailDTO rejectFastTrack(String ticketId, FastTrackRejectDTO dto) {
        Ticket ticket = findTicket(ticketId);
        AppUser me = currentAppUserService.getCurrentAppUser();
        if (!ticket.isFastTrack() || ticket.getBoardPost() == null) {
            throw new ConflictException("La segnalazione n. %d non ha una corsia veloce in attesa", ticket.getNumber());
        }
        requireTakenInCharge(ticket, "decidere sulla corsia veloce");
        return detail(fastTrackService.reject(ticket, dto.version(), dto.reason().strip()), me);
    }

    /**
     * Dentro la transizione: la mansione è a disposizione, di un'azienda attiva non esclusa per la segnalazione, e non ha
     * un'altra segnalazione dello stesso tipo di tirocinio (US-605); la assegna alla segnalazione e restituisce le scelte
     * da registrare. Il riassunto della mansione lo aggiorna {@link #afterProposal}.
     */
    private AuditChanges proposeSlot(Ticket t, JobSlot slot) {
        Company company = slot.getCompany();
        if (!slot.isActive() || !company.isActive()) {
            throw new ConflictException(
                    "La mansione «%s» non è più disponibile: è stata appena ritirata o modificata", slot.getTitle());
        }
        if (blacklistRepository.existsById(new TicketCompanyBlacklist.Id(t.getId(), company.getId()))) {
            throw new ConflictException("L'azienda %s è esclusa per questa segnalazione", company.getName());
        }
        jobSlotAssignments.requireAssignable(t, slot);
        t.setJobSlot(slot);
        t.setJobSlotHeld(true);
        return AuditChanges.of()
                .value("jobSlotId", null, TsidMapper.toExternal(slot.getId()))
                .value("companyId", null, TsidMapper.toExternal(company.getId()));
    }

    /**
     * Dopo la transizione verso la proposta: blocco della mansione nell'audit, annuncio della mansione archiviato con
     * il motivo, evento nello storico dell'azienda (brief, audit trail) e notifica al Tutor.
     */
    private void afterProposal(Ticket saved, JobSlot slot, String boardPostReason) {
        jobSlotAssignments.sync(slot, saved, true);
        boardPostService.archiveForUnavailableSlots(List.of(slot.getId()), boardPostReason);
        companyAuditEvents.save(new CompanyAuditEvent(slot.getCompany(), saved, CompanyEventType.PROPOSTA_INVIATA.name(),
                Map.of("ticketNumber", saved.getNumber(),
                        "jobSlotId", TsidMapper.toExternal(slot.getId()),
                        "jobSlotTitle", slot.getTitle())));
        notificationService.notify(saved.getTutor(), NotificationType.PROPOSAL_SENT, saved);
    }

    @Override
    public TicketDetailDTO accept(String ticketId, long expectedVersion) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "accettare la proposta");
        Instant started = clock.instant();
        LocalDate lastDay = workingDayService.plusWorkingDays(LocalDate.ofInstant(started, TimeConfig.ZONE),
                TIMER_WORKING_DAYS);
        // il timer scade alla fine del 7° giorno lavorativo (mezzanotte di Roma del giorno dopo)
        Instant deadline = lastDay.plusDays(1).atStartOfDay(TimeConfig.ZONE).toInstant();
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.ACCEPT, expectedVersion, null, t -> {
            t.setTimerStartedAt(started);
            t.setTimerDeadlineAt(deadline);
            return AuditChanges.of().value("timerDeadlineAt", null, deadline.toString());
        });
        JobSlot slot = saved.getJobSlot();
        companyAuditEvents.save(new CompanyAuditEvent(slot.getCompany(), saved,
                CompanyEventType.PROPOSTA_ACCOLTA.name(), Map.of("ticketNumber", saved.getNumber(),
                        "jobSlotTitle", slot.getTitle(), "timerLastDay", lastDay.toString())));
        notificationService.notify(saved.getAssignedCcOperator(), NotificationType.PROPOSAL_ACCEPTED, saved);
        return detail(saved, me);
    }

    @Override
    public TicketDetailDTO appointmentSet(String ticketId, AppointmentSetDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "registrare l'appuntamento");
        String note = dto.note() == null || dto.note().isBlank() ? null : dto.note().strip();
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.APPOINTMENT_SET, dto.version(), note, t -> {
            // il timer si ferma: i valori restano nell'audit
            AuditChanges changes = AuditChanges.of()
                    .value("timerDeadlineAt", toText(t.getTimerDeadlineAt()), null)
                    .value("timerReminderSentAt", toText(t.getTimerReminderSentAt()), null)
                    .value("timerStartedAt", toText(t.getTimerStartedAt()), null);
            t.setTimerStartedAt(null);
            t.setTimerReminderSentAt(null);
            t.setTimerDeadlineAt(null);
            return changes;
        });
        JobSlot slot = saved.getJobSlot();
        companyAuditEvents.save(new CompanyAuditEvent(slot.getCompany(), saved,
                CompanyEventType.APPUNTAMENTO_FISSATO.name(), Map.of("ticketNumber", saved.getNumber(),
                        "jobSlotTitle", slot.getTitle())));
        notificationService.notify(saved.getAssignedCcOperator(), NotificationType.APPOINTMENT_SET, saved);
        return detail(saved, me);
    }

    @Override
    public TicketDetailDTO startInternship(String ticketId, long expectedVersion) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "avviare il tirocinio");
        // dentro la transizione: prima si controllano versione e stato, poi la scheda
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.START, expectedVersion, null, t -> {
            internshipService.requireCompleteForStart(t);
            return AuditChanges.none();
        });
        JobSlot slot = saved.getJobSlot();
        companyAuditEvents.save(new CompanyAuditEvent(slot.getCompany(), saved,
                CompanyEventType.TIROCINIO_AVVIATO.name(), Map.of("ticketNumber", saved.getNumber(),
                        "jobSlotTitle", slot.getTitle())));
        notificationService.notify(saved.getAssignedCcOperator(), NotificationType.INTERNSHIP_STARTED, saved);
        return detail(saved, me);
    }

    @Override
    public TicketDetailDTO submitClosureReport(String ticketId, ClosureReportFormDTO form) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "inviare il report di restituzione");
        JobSlot slot = ticket.getJobSlot();
        // dentro la transizione: prima si controllano versione e stato, poi il report
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.CLOSE, form.version(), null, t -> {
            AuditChanges changes = closureReportService.create(t, form);
            // US-1102: se l'azienda offre ancora la mansione, si libera per questa segnalazione (US-605); altrimenti
            // l'assegnazione resta per il suo tipo. La segnalazione resta collegata alla mansione per lo storico
            if (form.jobSlotStillAvailable()) {
                t.setJobSlotHeld(false);
                changes.value("jobSlotReleased", null, true);
            }
            return changes;
        });
        if (form.jobSlotStillAvailable()) {
            jobSlotAssignments.sync(slot, saved, false);
        }
        ClosureReportDTO report = closureReportService.find(saved).orElseThrow();
        Map<String, Object> outcome = new LinkedHashMap<>();
        outcome.put("ticketNumber", saved.getNumber());
        outcome.put("jobSlotTitle", slot.getTitle());
        outcome.put("assunto", report.hired());
        outcome.put(report.hired() ? "formula" : "motivo",
                report.hired() ? report.hiringFormula().name() : report.noHireReason().name());
        outcome.put("mansioneDisponibile", report.jobSlotStillAvailable());
        companyAuditEvents.save(new CompanyAuditEvent(slot.getCompany(), saved,
                CompanyEventType.TIROCINIO_CONCLUSO.name(), outcome));
        notificationService.notify(saved.getAssignedCcOperator(), NotificationType.CLOSURE_REPORT_SUBMITTED, saved);
        return detail(saved, me);
    }

    @Override
    public TicketDetailDTO appointmentFailed(String ticketId, AppointmentFailedDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "registrare l'esito dell'appuntamento");
        String note = dto.note() == null || dto.note().isBlank() ? null : dto.note().strip();
        if (dto.cause() == AppointmentFailureCause.ALTRO && note == null) {
            throw new FieldValidationException("note", "Descrivi il motivo");
        }
        JobSlot slot = ticket.getJobSlot();
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.APPOINTMENT_FAILED, dto.version(), note, t -> {
            // la mansione si libera per questa segnalazione (US-605); nessuna esclusione automatica dell'azienda: decide
            // l'operatore (PO, 27/10)
            t.setJobSlotHeld(false);
            t.setJobSlot(null);
            AuditChanges changes = AuditChanges.of()
                    .value("cause", null, dto.cause().name())
                    .value("jobSlotId", TsidMapper.toExternal(slot.getId()), null);
            // la scheda del tirocinio riguardava quell'azienda: restano solo i dati del beneficiario (US-1001)
            if (internshipService.clearCompanyData(t)) {
                changes.value("internshipReset", null, true);
            }
            return dto.relaunchRequested() ? changes.value("boardRelaunch", null, true) : changes;
        });
        jobSlotAssignments.sync(slot, saved, false);
        companyAuditEvents.save(new CompanyAuditEvent(slot.getCompany(), saved,
                CompanyEventType.APPUNTAMENTO_FALLITO.name(), Map.of("ticketNumber", saved.getNumber(),
                        "jobSlotTitle", slot.getTitle(), "cause", dto.cause().name())));
        notificationService.notify(saved.getAssignedCcOperator(), NotificationType.APPOINTMENT_FAILED, saved);
        if (dto.relaunchRequested()) {
            // bozza riservata al progetto della segnalazione; pubblicarla resta una scelta del Call Center (US-803)
            boardPostService.createRelaunchDraft(slot, saved);
            notificationService.notify(saved.getAssignedCcOperator(), NotificationType.BOARD_RELAUNCH_REQUESTED, saved);
        }
        return detail(saved, me);
    }

    @Override
    public TicketDetailDTO rejectProposal(String ticketId, ProposalRejectDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "rifiutare la proposta");
        String reason = dto.reason().strip();
        JobSlot slot = ticket.getJobSlot();
        Company company = slot == null ? null : slot.getCompany();
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.PROPOSAL_REJECT, dto.version(), reason, t -> {
            t.setJobSlotHeld(false);
            t.setJobSlot(null);
            return AuditChanges.of()
                    .value("jobSlotId", TsidMapper.toExternal(slot.getId()), null)
                    .value("excludedCompanyId", null, TsidMapper.toExternal(company.getId()));
        });
        jobSlotAssignments.sync(slot, saved, false);
        // l'azienda non si ripropone per questa segnalazione (Q3); l'esclusione compare nello storico con il motivo
        if (!blacklistRepository.existsById(new TicketCompanyBlacklist.Id(saved.getId(), company.getId()))) {
            blacklistRepository.save(new TicketCompanyBlacklist(saved, company, reason));
        }
        companyAuditEvents.save(new CompanyAuditEvent(company, saved, CompanyEventType.PROPOSTA_RIFIUTATA.name(),
                Map.of("ticketNumber", saved.getNumber(), "jobSlotTitle", slot.getTitle())));
        notificationService.notify(saved.getAssignedCcOperator(), NotificationType.PROPOSAL_REJECTED, saved);
        return detail(saved, me);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyExclusionDTO> exclusions(String ticketId) {
        List<TicketCompanyBlacklist> rows = blacklistRepository.findByIdTicketId(findTicket(ticketId).getId());
        Map<String, String> names = userDisplayNameService.displayNames(
                rows.stream().map(TicketCompanyBlacklist::getCreatedBy).toList());
        return rows.stream()
                .sorted(Comparator.comparing(TicketCompanyBlacklist::getCreatedAt))
                .map(x -> new CompanyExclusionDTO(TsidMapper.toExternal(x.getCompany().getId()),
                        x.getCompany().getName(), x.getReason(), x.getCreatedAt(), names.get(x.getCreatedBy())))
                .toList();
    }

    @Override
    public List<CompanyExclusionDTO> excludeCompany(String ticketId, CompanyExclusionFormDTO dto) {
        Ticket ticket = findTicket(ticketId);
        if (ticket.getStatus() != TicketStatus.IN_LAVORAZIONE && ticket.getStatus() != TicketStatus.RIAPERTO) {
            throw new ConflictException("Si esclude un'azienda solo su una segnalazione in lavorazione o riaperta");
        }
        requireTakenInCharge(ticket, "escludere un'azienda");
        Company company = Optional.ofNullable(TsidMapper.toInternal(dto.companyId()))
                .flatMap(companyRepository::findById)
                .orElseThrow(() -> new FieldValidationException("companyId", "Azienda non valida"));
        if (blacklistRepository.existsById(new TicketCompanyBlacklist.Id(ticket.getId(), company.getId()))) {
            throw new ConflictException("%s è già esclusa per la segnalazione n. %d", company.getName(),
                    ticket.getNumber());
        }
        String reason = dto.reason().strip();
        blacklistRepository.saveAndFlush(new TicketCompanyBlacklist(ticket, company, reason));
        auditService.record(AuditEntityType.TICKET, ticket.getId(), "COMPANY_EXCLUDE",
                AuditChanges.of().value("excludedCompanyId", null, TsidMapper.toExternal(company.getId())), reason);
        return exclusions(ticketId);
    }

    /** Segnalazione visibile e del Tutor corrente: le azioni sulla proposta spettano solo a lui (PO, 27/10). */
    private Ticket ownTicket(String ticketId, AppUser me, String action) {
        Ticket ticket = visibleTicket(ticketId, me);
        if (!ticket.getTutor().getId().equals(me.getId())) {
            throw new AccessDeniedException("Solo il Tutor della segnalazione può " + action);
        }
        return ticket;
    }

    private static String toText(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    @Override
    public TicketDetailDTO takeOver(String ticketId, long expectedVersion) {
        Ticket ticket = findTicket(ticketId);
        AppUser me = currentAppUserService.getCurrentAppUser();
        if (ticket.getVersion() != expectedVersion) {
            throw new ConflictException("La segnalazione è stata modificata nel frattempo: ricarica la pagina e riprova");
        }
        AppUser previous = ticket.getAssignedCcOperator();
        if (ticket.getStatus() == TicketStatus.CLOSED || previous == null) {
            throw new ConflictException("La segnalazione n. %d non è in carico a nessuno: prendila dalla coda",
                    ticket.getNumber());
        }
        if (previous.getId().equals(me.getId())) {
            return detail(ticket, me);
        }
        ticket.setAssignedCcOperator(me);
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        auditService.record(AuditEntityType.TICKET, saved.getId(), "TAKE_OVER", AuditChanges.of()
                .value("assignedCcOperatorId", TsidMapper.toExternal(previous.getId()), TsidMapper.toExternal(me.getId())),
                null);
        return detail(saved, me);
    }

    private Ticket findTicket(String ticketId) {
        return Optional.ofNullable(TsidMapper.toInternal(ticketId))
                .flatMap(ticketRepository::findById)
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
    }

    /**
     * Una segnalazione si lavora dopo la presa in carico, da parte di qualunque operatore o dell'ADMIN, anche se è in
     * carico a un collega (US-405, decisione del PO del 05/10): l'assegnazione non cambia e l'audit registra chi agisce.
     */
    private static void requireTakenInCharge(Ticket ticket, String action) {
        if (ticket.getAssignedCcOperator() == null) {
            throw new ConflictException("Prendi in carico la segnalazione n. %d prima di %s", ticket.getNumber(), action);
        }
    }

    private TicketDetailDTO detail(Ticket t, AppUser me) {
        AppUser assigned = t.getAssignedCcOperator();
        Optional<BoardPost> relaunch = boardPostRepository.findFirstByCreatedFromTicket_IdOrderByNumberDesc(t.getId());
        return new TicketDetailDTO(
                TsidMapper.toExternal(t.getId()),
                t.getNumber(),
                t.getType(),
                t.getStatus(),
                t.isFastTrack(),
                t.getVersion(),
                beneficiaryMapper.toSummary(t.getBeneficiary()),
                referenceMapper.toDto(t.getBeneficiary().getResidenceZone()),
                referenceMapper.toDto(t.getProject()),
                referenceMapper.toDto(t.getRequestedJobCategory()),
                t.getRequestedJobFreeText(),
                fullName(t.getTutor()),
                assigned == null ? null : fullName(assigned),
                assigned != null && assigned.getId().equals(me.getId()),
                t.getJobSlot() == null ? null : jobSlotMapper.toMatch(t.getJobSlot()),
                t.getCreatedAt(),
                t.getTutor().getId().equals(me.getId()),
                t.getTimerDeadlineAt() == null ? null : TimeConfig.lastDay(t.getTimerDeadlineAt()),
                relaunch.map(p -> TsidMapper.toExternal(p.getId())).orElse(null),
                relaunch.map(BoardPost::getNumber).orElse(null),
                relaunch.map(BoardPost::getStatus).orElse(null),
                t.getBoardPost() == null ? null : TsidMapper.toExternal(t.getBoardPost().getId()),
                t.getBoardPost() == null ? null : t.getBoardPost().getNumber(),
                t.getStatus() == TicketStatus.FORM_RESTITUZIONE ? closureReportService.find(t).orElse(null) : null,
                referenceMapper.toDto(t.getCompetenceSite()),
                // il comando si propone solo se c'è una sede da scegliere (controllo in Chrome, 10/11)
                canChangeCompetenceSite(t, me)
                        && competenceSiteRepository.existsActiveForProject(t.getProject().getId()),
                referenceMapper.toDto(t.getInternshipType()),
                canChangeInternshipType(t, me),
                canChangeInternshipType(t, me)
                        ? internshipTypeRules.choosable(t.getProject(), t.getInternshipType()) : List.of(),
                canCorrectRequest(t, me),
                canCorrectRequest(t, me) && canCorrectProject(t));
    }

    /** Stati prima della proposta all'azienda (US-309): progetto e mansione si correggono ancora. */
    private static final Set<TicketStatus> BEFORE_PROPOSAL =
            Set.of(TicketStatus.NUOVA, TicketStatus.IN_ATTESA_APPROVAZIONE, TicketStatus.IN_LAVORAZIONE);

    /**
     * Solo il Tutor della segnalazione normale (US-309, PO 28/11): prima della proposta, oppure riaperta per cercare
     * un'altra mansione.
     */
    private static boolean canCorrectRequest(Ticket t, AppUser me) {
        return t.getTutor().getId().equals(me.getId()) && t.getType() == TicketType.NORMAL
                && (BEFORE_PROPOSAL.contains(t.getStatus()) || t.getStatus() == TicketStatus.RIAPERTO);
    }

    /** Il progetto si corregge solo prima della proposta; nella riaperta resta (PO, 28/11). */
    private static boolean canCorrectProject(Ticket t) {
        return BEFORE_PROPOSAL.contains(t.getStatus());
    }

    /** Il Tutor della segnalazione, fino all'avvio del tirocinio (US-605). */
    private static boolean canChangeInternshipType(Ticket t, AppUser me) {
        return t.getTutor().getId().equals(me.getId())
                && !TicketInternshipTypeServiceImpl.STARTED.contains(t.getStatus());
    }

    private static String fullName(AppUser user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private static Long filterId(String externalId, String what) {
        if (externalId == null || externalId.isBlank()) {
            return null;
        }
        Long id = TsidMapper.toInternal(externalId);
        if (id == null) {
            throw new BadRequestException("Filtro %s non valido", what);
        }
        return id;
    }

    // inesistente e altrui danno lo stesso errore: non si rivela l'esistenza dei beneficiari degli altri tutor
    private Beneficiary ownBeneficiary(String beneficiaryId, AppUser me) {
        Beneficiary beneficiary = Optional.ofNullable(TsidMapper.toInternal(beneficiaryId))
                .flatMap(beneficiaryRepository::findById)
                .filter(c -> c.getOwnerTutor().getId().equals(me.getId()))
                .orElseThrow(() -> new FieldValidationException("beneficiaryId", "Beneficiario non valido"));
        if (beneficiary.getAnonymizedAt() != null) {
            throw new FieldValidationException("beneficiaryId", "Il beneficiario è anonimizzato: non si segnala più");
        }
        // nuova attività: non è più da anonimizzare (US-1303)
        beneficiary.clearAnonymizationDue();
        return beneficiary;
    }

    /** Annuncio della bacheca visibile al Tutor: per tutti o riservato a un suo progetto; altrimenti 404. */
    private BoardPost visibleBoardPost(String boardPostId, AppUser me) {
        return Optional.ofNullable(TsidMapper.toInternal(boardPostId))
                .flatMap(boardPostRepository::findById)
                // una bozza non è in bacheca: per il Tutor non esiste
                .filter(p -> p.getStatus() != BoardPostStatus.DRAFT)
                .filter(p -> p.getProject() == null
                        || userProjectRepository.existsById(new UserProject.Id(me.getId(), p.getProject().getId())))
                .orElseThrow(() -> new NotFoundException("Annuncio non trovato"));
    }

    private Project assignedProject(String projectId, AppUser me) {
        return Optional.ofNullable(TsidMapper.toInternal(projectId))
                .filter(id -> userProjectRepository.existsById(new UserProject.Id(me.getId(), id)))
                .flatMap(projectRepository::findById)
                .filter(Project::isActive)
                .orElseThrow(() -> new FieldValidationException("projectId", "Progetto non valido o non assegnato"));
    }

    private JobCategory activeJobCategory(String jobCategoryId) {
        return Optional.ofNullable(TsidMapper.toInternal(jobCategoryId))
                .flatMap(jobCategoryRepository::findById)
                .filter(JobCategory::isActive)
                .orElseThrow(() -> new FieldValidationException("jobCategoryId", "Mansione non valida"));
    }
}
