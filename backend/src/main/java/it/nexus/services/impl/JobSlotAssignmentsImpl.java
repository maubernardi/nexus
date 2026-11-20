package it.nexus.services.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.JobSlot;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.JobSlotAssignmentDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.TicketRepository;
import it.nexus.services.AuditService;
import it.nexus.services.JobSlotAssignments;
import it.nexus.web.errors.ConflictException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class JobSlotAssignmentsImpl implements JobSlotAssignments {

    private final TicketRepository ticketRepository;
    private final AuditService auditService;
    private final ReferenceMapper referenceMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> holders(JobSlot slot) {
        return ticketRepository.findHolders(slot.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<JobSlotAssignmentDTO>> assignments(Collection<Long> jobSlotIds) {
        if (jobSlotIds.isEmpty()) {
            return Map.of();
        }
        return ticketRepository.findHolders(jobSlotIds).stream().collect(Collectors.groupingBy(
                t -> t.getJobSlot().getId(), LinkedHashMap::new,
                Collectors.mapping(t -> new JobSlotAssignmentDTO(t.getNumber(),
                        referenceMapper.toDto(t.getInternshipType())), Collectors.toList())));
    }

    @Override
    public void requireAssignable(Ticket ticket, JobSlot slot) {
        if (ticket.getInternshipType() == null) {
            throw new ConflictException("La segnalazione n. %d non ha il tipo di tirocinio: lo indica il Tutor",
                    ticket.getNumber());
        }
        // proposte della stessa mansione in fila: la seconda trova la versione cambiata (409)
        entityManager.lock(slot, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        List<Ticket> conflicting = holders(slot).stream()
                .filter(t -> !t.equals(ticket))
                .filter(t -> t.getInternshipType() == null || t.getInternshipType().equals(ticket.getInternshipType()))
                .toList();
        if (!conflicting.isEmpty()) {
            throw new ConflictException("La mansione «%s» è già assegnata alla segnalazione %s", slot.getTitle(),
                    describe(conflicting));
        }
    }

    @Override
    public void sync(JobSlot slot, Ticket ticket, boolean assigned) {
        JobSlotStatus before = slot.getStatus();
        List<Ticket> holders = holders(slot);
        slot.summarize(holders.isEmpty() ? null : holders.getFirst());
        String id = TsidMapper.toExternal(ticket.getId());
        String type = ticket.getInternshipType() == null ? null : ticket.getInternshipType().getCode();
        AuditChanges changes = AuditChanges.of().value("status", before.name(), slot.getStatus().name());
        changes = assigned
                ? changes.value("ticketId", null, id).value("internshipType", null, type)
                : changes.value("ticketId", id, null).value("internshipType", type, null);
        auditService.record(AuditEntityType.JOB_SLOT, slot.getId(), assigned ? "BLOCK" : "RELEASE", changes, null);
    }

    @Override
    public String describe(List<Ticket> holders) {
        return holders.stream()
                .map(t -> "n. %d (%s)".formatted(t.getNumber(), t.getInternshipType() == null
                        ? "tipo non indicato" : t.getInternshipType().getName()))
                .collect(Collectors.joining(", "));
    }
}
