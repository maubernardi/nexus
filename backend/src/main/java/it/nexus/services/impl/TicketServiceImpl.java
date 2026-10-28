package it.nexus.services.impl;

import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketCompanyBlacklist;
import it.nexus.domain.TicketStatusHistory;
import it.nexus.domain.UserProject;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.AppointmentFailedDTO;
import it.nexus.domain.dto.AppointmentSetDTO;
import it.nexus.domain.dto.ApproveCategoryDTO;
import it.nexus.domain.dto.CompanyExclusionDTO;
import it.nexus.domain.dto.CompanyExclusionFormDTO;
import it.nexus.domain.dto.JobSlotMatchDTO;
import it.nexus.domain.dto.MatchRequestDTO;
import it.nexus.domain.dto.ProposalRejectDTO;
import it.nexus.domain.dto.RejectCategoryDTO;
import it.nexus.domain.dto.FastTrackRejectDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketSpecialCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.dto.TicketDetailDTO;
import it.nexus.domain.dto.TicketEventDTO;
import it.nexus.domain.dto.TicketListItemDTO;
import it.nexus.domain.enumeration.AppointmentFailureCause;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.CompanyEventType;
import it.nexus.domain.enumeration.JobSlotStatus;
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
import it.nexus.services.FastTrackService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.NotificationService;
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
    private final WorkingDayService workingDayService;
    private final NotificationService notificationService;
    private final Clock clock;
    private final JobSlotRepository jobSlotRepository;
    private final TicketCompanyBlacklistRepository blacklistRepository;
    private final CompanyRepository companyRepository;
    private final CompanyAuditEventRepository companyAuditEvents;
    private final AuditService auditService;
    private final BoardPostService boardPostService;
    private final FastTrackService fastTrackService;
    private final BoardPostRepository boardPostRepository;
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
        Ticket ticket = new Ticket(me, project, beneficiary, TicketType.NORMAL);
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
        ticket.setFastTrack(true);
        ticket.setBoardPost(post);
        ticket.setRequestedJobCategory(post.getJobSlot().getJobCategory());
        return mapper.toDto(stateMachine.create(ticket, TicketTransitions.SUBMIT_SPECIAL, null));
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
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        base = base.isEmpty() ? "TIPOLOGIA" : base.substring(0, Math.min(base.length(), 26));
        String code = base;
        for (int i = 2; jobCategoryRepository.existsByCode(code); i++) {
            code = base + "_" + i;
        }
        return code;
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
            case TO_APPROVE -> {
                spec = spec.and(TicketSpecifications.status(TicketStatus.IN_ATTESA_APPROVAZIONE));
                yield oldestFirst;
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
     * Dentro la transizione: la mansione è libera, a disposizione e di un'azienda attiva non esclusa per la segnalazione;
     * la blocca per la segnalazione e restituisce le scelte da registrare.
     */
    private AuditChanges proposeSlot(Ticket t, JobSlot slot) {
        Company company = slot.getCompany();
        if (slot.getStatus() != JobSlotStatus.LIBERA || !slot.isActive() || !company.isActive()) {
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
    }

    /**
     * Dopo la transizione verso la proposta: blocco della mansione nell'audit, annuncio della mansione archiviato con
     * il motivo, evento nello storico dell'azienda (brief, audit trail) e notifica al Tutor.
     */
    private void afterProposal(Ticket saved, JobSlot slot, String boardPostReason) {
        auditService.record(AuditEntityType.JOB_SLOT, slot.getId(), "BLOCK", AuditChanges.of()
                .value("status", JobSlotStatus.LIBERA.name(), JobSlotStatus.BLOCCATA.name())
                .value("blockedByTicketId", null, TsidMapper.toExternal(saved.getId())), null);
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
    public TicketDetailDTO appointmentFailed(String ticketId, AppointmentFailedDTO dto) {
        AppUser me = currentAppUserService.getCurrentAppUser();
        Ticket ticket = ownTicket(ticketId, me, "registrare l'esito dell'appuntamento");
        String note = dto.note() == null || dto.note().isBlank() ? null : dto.note().strip();
        if (dto.cause() == AppointmentFailureCause.ALTRO && note == null) {
            throw new FieldValidationException("note", "Descrivi il motivo");
        }
        JobSlot slot = ticket.getJobSlot();
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.APPOINTMENT_FAILED, dto.version(), note, t -> {
            // la mansione torna libera; nessuna esclusione automatica dell'azienda: decide l'operatore (PO, 27/10)
            slot.release();
            t.setJobSlot(null);
            AuditChanges changes = AuditChanges.of()
                    .value("cause", null, dto.cause().name())
                    .value("jobSlotId", TsidMapper.toExternal(slot.getId()), null);
            return dto.relaunchRequested() ? changes.value("boardRelaunch", null, true) : changes;
        });
        auditService.record(AuditEntityType.JOB_SLOT, slot.getId(), "RELEASE", AuditChanges.of()
                .value("status", JobSlotStatus.BLOCCATA.name(), JobSlotStatus.LIBERA.name())
                .value("blockedByTicketId", TsidMapper.toExternal(saved.getId()), null), null);
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
            slot.release();
            t.setJobSlot(null);
            return AuditChanges.of()
                    .value("jobSlotId", TsidMapper.toExternal(slot.getId()), null)
                    .value("excludedCompanyId", null, TsidMapper.toExternal(company.getId()));
        });
        auditService.record(AuditEntityType.JOB_SLOT, slot.getId(), "RELEASE", AuditChanges.of()
                .value("status", JobSlotStatus.BLOCCATA.name(), JobSlotStatus.LIBERA.name())
                .value("blockedByTicketId", TsidMapper.toExternal(saved.getId()), null), null);
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
                t.getBoardPost() == null ? null : t.getBoardPost().getNumber());
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
