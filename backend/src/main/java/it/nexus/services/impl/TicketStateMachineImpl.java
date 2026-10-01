package it.nexus.services.impl;

import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.SecurityUtils;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketStatusHistory;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.workflow.TicketTransition;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.services.TicketStateMachine;
import it.nexus.web.errors.ConflictException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketStateMachineImpl implements TicketStateMachine {

    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository historyRepository;

    @Override
    public Ticket create(Ticket ticket, TicketTransition transition, String note) {
        if (!transition.isCreation() || ticket.getId() != null) {
            throw new IllegalArgumentException("La transizione " + transition.name() + " non crea un ticket");
        }
        requireRole(transition);
        ticket.setStatus(transition.to());
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        historyRepository.save(new TicketStatusHistory(saved, null, transition.to(), blankToNull(note)));
        return saved;
    }

    @Override
    public Ticket apply(Ticket ticket, TicketTransition transition, long expectedVersion, String note) {
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
        ticket.setStatus(transition.to());
        // flush immediato: un conflitto di versione emerge qui (409) e non al commit
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        historyRepository.save(new TicketStatusHistory(saved, from, transition.to(), blankToNull(note)));
        return saved;
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
