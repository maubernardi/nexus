package it.nexus.services.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Company;
import it.nexus.domain.CompanyAuditEvent;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketCompanyBlacklist;
import it.nexus.domain.UserProject;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.JobSlotMatchDTO;
import it.nexus.domain.dto.MatchRequestDTO;
import it.nexus.domain.dto.QueueItemDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.dto.TicketDetailDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;
import it.nexus.domain.workflow.TicketTransitions;
import it.nexus.mapper.BeneficiaryMapper;
import it.nexus.mapper.JobSlotMapper;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TicketMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketCompanyBlacklistRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.TicketService;
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

    /** Stati da lavorare: nuove segnalazioni e speciali non ancora prese in carico. */
    private static final Set<TicketStatus> QUEUE_STATUSES = EnumSet.of(TicketStatus.NUOVA, TicketStatus.IN_LAVORAZIONE);

    private final TicketRepository ticketRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final CurrentAppUserService currentAppUserService;
    private final TicketStateMachine stateMachine;
    private final TicketMapper mapper;
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
    @Transactional(readOnly = true)
    public List<QueueItemDTO> queue(String projectId, String zoneId) {
        return ticketRepository.findQueue(QUEUE_STATUSES, filterId(projectId, "progetto"), filterId(zoneId, "zona"))
                .stream()
                .map(mapper::toQueueItem)
                .toList();
    }

    @Override
    public QueueItemDTO takeCharge(String ticketId, long expectedVersion) {
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
        return mapper.toQueueItem(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueItemDTO> assignedToMe() {
        AppUser me = currentAppUserService.getCurrentAppUser();
        return ticketRepository.findAssignedTo(me.getId(), TicketStatus.CLOSED).stream().map(mapper::toQueueItem).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailDTO getForWork(String ticketId) {
        return detail(findTicket(ticketId), currentAppUserService.getCurrentAppUser());
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
            requireWorkingOperator(t, me);
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
        companyAuditEvents.save(new CompanyAuditEvent(company, saved, "PROPOSTA_INVIATA", Map.of(
                "ticketNumber", saved.getNumber(),
                "jobSlotId", TsidMapper.toExternal(slot.getId()),
                "jobSlotTitle", slot.getTitle())));
        return detail(saved, me);
    }

    private Ticket findTicket(String ticketId) {
        return Optional.ofNullable(TsidMapper.toInternal(ticketId))
                .flatMap(ticketRepository::findById)
                .orElseThrow(() -> new NotFoundException("Segnalazione non trovata"));
    }

    /** Lavora la segnalazione solo l'operatore che l'ha in carico; l'ADMIN può intervenire sempre. */
    private static void requireWorkingOperator(Ticket ticket, AppUser me) {
        if (me.getRole() == Role.ADMIN) {
            return;
        }
        AppUser assigned = ticket.getAssignedCcOperator();
        if (assigned == null) {
            throw new ConflictException("Prendi in carico la segnalazione n. %d prima di abbinarla", ticket.getNumber());
        }
        if (!assigned.getId().equals(me.getId())) {
            throw new ConflictException("La segnalazione n. %d è in carico a un collega", ticket.getNumber());
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
