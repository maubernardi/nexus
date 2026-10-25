package it.nexus.services.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.SecurityUtils;
import it.nexus.domain.AppUser;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyAuditEvent;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketCompanyBlacklist;
import it.nexus.domain.TicketStatusHistory;
import it.nexus.domain.UserProject;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.JobSlotMatchDTO;
import it.nexus.domain.dto.MatchRequestDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.dto.TicketDetailDTO;
import it.nexus.domain.dto.TicketEventDTO;
import it.nexus.domain.dto.TicketListItemDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.CompanyEventType;
import it.nexus.domain.enumeration.JobSlotStatus;
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
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketCompanyBlacklistRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketSpecifications;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.TicketService;
import it.nexus.services.UserDisplayNameService;
import it.nexus.services.TicketStateMachine;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    /** Stati della coda: nuove segnalazioni e speciali non ancora prese in carico. */
    private static final Set<TicketStatus> QUEUE_STATUSES = EnumSet.of(TicketStatus.NUOVA, TicketStatus.IN_LAVORAZIONE);

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
    private final JobSlotRepository jobSlotRepository;
    private final TicketCompanyBlacklistRepository blacklistRepository;
    private final CompanyAuditEventRepository companyAuditEvents;
    private final AuditService auditService;
    private final JobSlotMapper jobSlotMapper;
    private final BeneficiaryMapper beneficiaryMapper;
    private final ReferenceMapper referenceMapper;

    @Override
    public TicketDTO submit(TicketCreateDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Beneficiary beneficiary = ownBeneficiary(dto.beneficiaryId(), me);
        ticketRepository.findFirstByBeneficiaryIdAndStatusNot(beneficiary.getId(), TicketStatus.CLOSED).ifPresent(open -> {
            throw new FieldValidationException("beneficiaryId",
                    "Il beneficiario ha già una segnalazione aperta (n. %d)".formatted(open.getNumber()));
        });
        Project project = assignedProject(dto.projectId(), me);
        JobCategory category = activeJobCategory(dto.jobCategoryId());

        Ticket ticket = new Ticket(me, project, beneficiary, TicketType.NORMAL);
        ticket.setRequestedJobCategory(category);
        return mapper.toDto(stateMachine.create(ticket, TicketTransitions.SUBMIT, null));
    }

    @Override
    public TicketListItemDTO takeCharge(String ticketId, long expectedVersion) {
        Ticket ticket = Optional.ofNullable(TsidMapper.toInternal(ticketId))
                .flatMap(ticketRepository::findById)
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
        AppUser me = currentAppUserService.getCurrentAppUser();
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
            case TO_TAKE -> {
                spec = spec.and(TicketSpecifications.statusIn(QUEUE_STATUSES)).and(TicketSpecifications.unassigned());
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
        return ticketRepository.findAll(spec, sort).stream().map(mapper::toListItem).toList();
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

    /** Dettaglio leggibile di un evento: chi ha preso in carico, a chi è passata, quale proposta. */
    private String describe(AuditEvent event) {
        return switch (event.getAction()) {
            case "TAKE_CHARGE" -> userName(changed(event, "assignedCcOperatorId", "after"));
            case "TAKE_OVER" -> "%s → %s".formatted(userName(changed(event, "assignedCcOperatorId", "before")),
                    userName(changed(event, "assignedCcOperatorId", "after")));
            case "MATCH" -> Optional.ofNullable(TsidMapper.toInternal(changed(event, "jobSlotId", "after")))
                    .flatMap(jobSlotRepository::findById)
                    .map(slot -> "«%s» presso %s".formatted(slot.getTitle(), slot.getCompany().getName()))
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

    private static String changed(AuditEvent event, String field, String side) {
        Object change = event.getChanges().get(field);
        return change instanceof Map<?, ?> map && map.get(side) != null ? map.get(side).toString() : null;
    }

    private static TicketStatus status(AuditEvent event, String side) {
        String value = changed(event, "status", side);
        return value == null ? null : TicketStatus.valueOf(value);
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
        return jobSlotRepository.findCompatible(ticket.getId(), filterId(zoneId, "zona"), filterId(jobCategoryId, "tipologia"))
                .stream()
                .map(jobSlotMapper::toMatch)
                .toList();
    }

    @Override
    public TicketDetailDTO match(String ticketId, MatchRequestDTO request) {
        Ticket ticket = findTicket(ticketId);
        AppUser me = currentAppUserService.getCurrentAppUser();
        JobSlot slot = Optional.ofNullable(TsidMapper.toInternal(request.jobSlotId()))
                .flatMap(jobSlotRepository::findById)
                .orElseThrow(() -> new FieldValidationException("jobSlotId", "Mansione non valida"));
        Company company = slot.getCompany();

        Ticket saved = stateMachine.apply(ticket, TicketTransitions.MATCH, request.ticketVersion(), null, t -> {
            requireTakenInCharge(t);
            if (slot.getVersion() != request.jobSlotVersion() || slot.getStatus() != JobSlotStatus.LIBERA
                    || !slot.isActive() || !company.isActive()) {
                throw new ConflictException(
                        "La mansione «%s» non è più disponibile: è stata appena bloccata o modificata", slot.getTitle());
            }
            if (blacklistRepository.existsById(new TicketCompanyBlacklist.Id(t.getId(), company.getId()))) {
                throw new ConflictException("L'azienda %s è esclusa per questa segnalazione", company.getName());
            }
            t.setJobSlot(slot);
            slot.blockFor(t);
            // flush subito: se un'altra segnalazione ha appena bloccato la mansione, il conflitto emerge qui (409)
            jobSlotRepository.saveAndFlush(slot);
            return AuditChanges.of()
                    .value("jobSlotId", null, TsidMapper.toExternal(slot.getId()))
                    .value("companyId", null, TsidMapper.toExternal(company.getId()));
        });

        auditService.record(AuditEntityType.JOB_SLOT, slot.getId(), "BLOCK", AuditChanges.of()
                .value("status", JobSlotStatus.LIBERA.name(), JobSlotStatus.BLOCCATA.name())
                .value("blockedByTicketId", null, TsidMapper.toExternal(saved.getId())), null);
        // storico dell'azienda: la proposta è un evento del rapporto con l'azienda (brief, audit trail)
        companyAuditEvents.save(new CompanyAuditEvent(company, saved, CompanyEventType.PROPOSTA_INVIATA.name(), Map.of(
                "ticketNumber", saved.getNumber(),
                "jobSlotId", TsidMapper.toExternal(slot.getId()),
                "jobSlotTitle", slot.getTitle())));
        return detail(saved, me);
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
    private static void requireTakenInCharge(Ticket ticket) {
        if (ticket.getAssignedCcOperator() == null) {
            throw new ConflictException("Prendi in carico la segnalazione n. %d prima di abbinarla", ticket.getNumber());
        }
    }

    private TicketDetailDTO detail(Ticket t, AppUser me) {
        AppUser assigned = t.getAssignedCcOperator();
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
                t.getCreatedAt());
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
        return Optional.ofNullable(TsidMapper.toInternal(beneficiaryId))
                .flatMap(beneficiaryRepository::findById)
                .filter(c -> c.getOwnerTutor().getId().equals(me.getId()))
                .orElseThrow(() -> new FieldValidationException("beneficiaryId", "Beneficiario non valido"));
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
