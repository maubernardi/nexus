package it.nexus.services.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.JobCategory;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.UserProject;
import it.nexus.domain.dto.QueueItemDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;
import it.nexus.domain.workflow.TicketTransitions;
import it.nexus.mapper.TicketMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.TicketService;
import it.nexus.services.TicketStateMachine;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.FieldValidationException;
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
