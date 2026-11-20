package it.nexus.services.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.InternshipType;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.repository.TicketRepository;
import it.nexus.services.AuditService;
import it.nexus.services.InternshipTypeRules;
import it.nexus.services.JobSlotAssignments;
import it.nexus.services.TicketInternshipTypeService;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketInternshipTypeServiceImpl implements TicketInternshipTypeService {

    /** Dall'avvio in poi il tipo non cambia (US-605). */
    static final Set<TicketStatus> STARTED = EnumSet.of(TicketStatus.IN_TIROCINIO, TicketStatus.FORM_RESTITUZIONE);
    static final String AFTER_START = "Il tipo di tirocinio non si cambia dopo l'avvio";

    private final TicketRepository ticketRepository;
    private final JobSlotAssignments jobSlotAssignments;
    private final AuditService auditService;
    private final EntityManager entityManager;

    @Override
    public boolean change(Ticket ticket, InternshipType type, boolean fromInternship) {
        InternshipType before = ticket.getInternshipType();
        if (type.equals(before)) {
            return false;
        }
        if (STARTED.contains(ticket.getStatus())) {
            throw fromInternship ? new FieldValidationException(InternshipTypeRules.FIELD, AFTER_START)
                    : new ConflictException(AFTER_START);
        }
        if (ticket.isJobSlotHeld()) {
            JobSlot slot = ticket.getJobSlot();
            // l'assegnazione cambia tipo: in fila con le proposte della stessa mansione
            entityManager.lock(slot, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
            List<Ticket> conflicting = jobSlotAssignments.holders(slot).stream()
                    .filter(t -> !t.equals(ticket))
                    .filter(t -> t.getInternshipType() == null || t.getInternshipType().equals(type))
                    .toList();
            if (!conflicting.isEmpty()) {
                throw new FieldValidationException(InternshipTypeRules.FIELD,
                        "Sulla mansione «%s» c'è già la segnalazione %s".formatted(slot.getTitle(),
                                jobSlotAssignments.describe(conflicting)));
            }
        }
        ticket.setInternshipType(type);
        ticketRepository.saveAndFlush(ticket);
        auditService.record(AuditEntityType.TICKET, ticket.getId(), "INTERNSHIP_TYPE_CHANGED", AuditChanges.of()
                .value("internshipType", before == null ? null : before.getName(), type.getName()), null);
        return true;
    }
}
