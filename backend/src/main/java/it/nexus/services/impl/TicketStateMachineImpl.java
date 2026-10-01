package it.nexus.services.impl;

import java.util.Set;
import java.util.function.Function;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.SecurityUtils;
import it.nexus.domain.AbstractTsidEntity;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketStatusHistory;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.workflow.TicketTransition;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.services.AuditService;
import it.nexus.services.TicketStateMachine;
import it.nexus.web.errors.ConflictException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketStateMachineImpl implements TicketStateMachine {

    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository historyRepository;
    private final AuditService auditService;

    @Override
    public Ticket create(Ticket ticket, TicketTransition transition, String note) {
        if (!transition.isCreation() || ticket.getId() != null) {
            throw new IllegalArgumentException("La transizione " + transition.name() + " non crea un ticket");
        }
        requireRole(transition);
        ticket.setStatus(transition.to());
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        historyRepository.save(new TicketStatusHistory(saved, null, transition.to(), blankToNull(note)));
        // alla creazione si registrano anche le scelte fatte
        auditService.record(AuditEntityType.TICKET, saved.getId(), transition.name(), AuditChanges.of()
                .value("status", null, transition.to().name())
                .value("type", null, saved.getType().name())
                .value("beneficiaryId", null, external(saved.getBeneficiary()))
                .value("projectId", null, external(saved.getProject()))
                .value("requestedJobCategoryId", null, external(saved.getRequestedJobCategory()))
                .value("requestedJobFreeText", null, saved.getRequestedJobFreeText()), note);
        return saved;
    }

    @Override
    public Ticket apply(Ticket ticket, TicketTransition transition, long expectedVersion, String note) {
        return apply(ticket, transition, expectedVersion, note, t -> AuditChanges.none());
    }

    @Override
    public Ticket apply(Ticket ticket, TicketTransition transition, long expectedVersion, String note,
            Function<Ticket, AuditChanges> effects) {
        if (transition.isCreation()) {
            throw new IllegalArgumentException("La transizione " + transition.name() + " è di creazione");
        }
        requireRole(transition);
        if (ticket.getVersion() != expectedVersion) {
            throw new ConflictException("Il ticket è stato modificato nel frattempo: ricarica la pagina e riprova");
        }
        TicketStatus from = ticket.getStatus();
        if (!transition.allowedFrom(from)) {
            throw new ConflictException("Operazione non ammessa per un ticket in stato %s", from);
        }
        AuditChanges changes = AuditChanges.of().value("status", from.name(), transition.to().name())
                .merge(effects.apply(ticket));
        ticket.setStatus(transition.to());
        // flush immediato: un conflitto di versione emerge qui (409) e non al commit
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        historyRepository.save(new TicketStatusHistory(saved, from, transition.to(), blankToNull(note)));
        auditService.record(AuditEntityType.TICKET, saved.getId(), transition.name(), changes, note);
        return saved;
    }

    private static String external(AbstractTsidEntity entity) {
        return entity == null ? null : TsidMapper.toExternal(entity.getId());
    }

    private static void requireRole(TicketTransition transition) {
        Set<Role> roles = SecurityUtils.getCurrentUser()
                .map(AuthenticatedUser::roles)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Nessun utente autenticato"));
        if (!transition.allowedFor(roles)) {
            throw new AccessDeniedException("Operazione non consentita al ruolo");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
