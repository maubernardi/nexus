package it.nexus.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.UserProject;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.domain.dto.TicketTransferDTO;
import it.nexus.domain.dto.TicketTransferResultDTO;
import it.nexus.domain.dto.TransferGroupDTO;
import it.nexus.domain.dto.TutorOptionDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TicketMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.NotificationService;
import it.nexus.services.TicketTransferService;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TicketTransferServiceImpl implements TicketTransferService {

    static final String ACTION = "TRANSFER";

    private final AppUserRepository appUserRepository;
    private final TicketRepository ticketRepository;
    private final UserProjectRepository userProjectRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TransferGroupDTO> openTickets(String tutorId) {
        AppUser tutor = Optional.ofNullable(TsidMapper.toInternal(tutorId)).flatMap(appUserRepository::findById)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        Map<Project, List<Ticket>> byProject = ticketRepository
                .findByTutorIdAndStatusNotOrderByNumberAsc(tutor.getId(), TicketStatus.CLOSED).stream()
                .collect(Collectors.groupingBy(Ticket::getProject,
                        () -> new TreeMap<>(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(Project::getId)),
                        Collectors.toList()));
        return byProject.entrySet().stream().map(e -> new TransferGroupDTO(
                new ReferenceItemDTO(TsidMapper.toExternal(e.getKey().getId()), e.getKey().getCode(), e.getKey().getName()),
                e.getValue().stream().map(ticketMapper::toListItem).toList(),
                userProjectRepository.findActiveTutorsOf(e.getKey().getId()).stream()
                        .filter(u -> !u.getId().equals(tutor.getId()))
                        .map(u -> new TutorOptionDTO(TsidMapper.toExternal(u.getId()), fullName(u)))
                        .toList()))
                .toList();
    }

    @Override
    public TicketTransferResultDTO transfer(TicketTransferDTO dto) {
        AppUser target = Optional.ofNullable(TsidMapper.toInternal(dto.tutorId())).flatMap(appUserRepository::findById)
                .filter(u -> u.getRole() == Role.TUTOR && u.isActive())
                .orElseThrow(() -> new FieldValidationException("tutorId", "Scegli un Tutor attivo"));
        Set<Long> ids = new LinkedHashSet<>();
        for (String id : dto.ticketIds()) {
            ids.add(Optional.ofNullable(TsidMapper.toInternal(id))
                    .orElseThrow(() -> new FieldValidationException("ticketIds", "Segnalazione non valida")));
        }
        List<Ticket> tickets = new ArrayList<>(ticketRepository.findAllById(ids));
        if (tickets.size() != ids.size()) {
            throw new FieldValidationException("ticketIds", "Segnalazione non valida");
        }
        Project project = tickets.getFirst().getProject();
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.CLOSED) {
                throw new BadRequestException("La segnalazione n. %d è chiusa: non si sposta".formatted(ticket.getNumber()));
            }
            if (!ticket.getProject().getId().equals(project.getId())) {
                throw new BadRequestException("Le segnalazioni da spostare insieme devono essere dello stesso progetto");
            }
            if (ticket.getTutor().getId().equals(target.getId())) {
                throw new FieldValidationException("tutorId",
                        "La segnalazione n. %d è già di questo Tutor".formatted(ticket.getNumber()));
            }
        }
        if (!userProjectRepository.existsById(new UserProject.Id(target.getId(), project.getId()))) {
            throw new FieldValidationException("tutorId",
                    "Il Tutor scelto non è assegnato al progetto " + project.getName());
        }

        String targetId = TsidMapper.toExternal(target.getId());
        tickets.sort(Comparator.comparing(Ticket::getNumber));
        for (Ticket ticket : tickets) {
            String previous = TsidMapper.toExternal(ticket.getTutor().getId());
            ticket.setTutor(target);
            auditService.record(AuditEntityType.TICKET, ticket.getId(), ACTION,
                    AuditChanges.of().value("tutorId", previous, targetId), null);
            // il beneficiario segue la segnalazione aperta (decisione del PO, 06/11); le chiuse restano nello storico
            Beneficiary beneficiary = ticket.getBeneficiary();
            if (!beneficiary.getOwnerTutor().getId().equals(target.getId())) {
                String owner = TsidMapper.toExternal(beneficiary.getOwnerTutor().getId());
                beneficiary.setOwnerTutor(target);
                auditService.record(AuditEntityType.BENEFICIARY, beneficiary.getId(), ACTION,
                        AuditChanges.of().value("ownerTutorId", owner, targetId), null);
            }
        }
        ticketRepository.saveAllAndFlush(tickets);
        tickets.forEach(t -> notificationService.notify(target, NotificationType.TICKET_TRANSFERRED, t));
        log.info("{} segnalazioni del progetto {} spostate su {}", tickets.size(), project.getId(), target);
        return new TicketTransferResultDTO(tickets.size(), fullName(target));
    }

    private static String fullName(AppUser user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
