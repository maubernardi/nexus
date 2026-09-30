package it.nexus.domain.workflow;

import java.util.Set;

import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;

/**
 * Transizione ammessa della macchina a stati del ticket: da quali stati parte, in quale arriva e quali ruoli possono
 * richiederla. Con {@code from} vuoto è una transizione di creazione (stato iniziale del ticket).
 */
public record TicketTransition(String name, Set<TicketStatus> from, TicketStatus to, Set<Role> roles) {

    public TicketTransition {
        from = Set.copyOf(from);
        roles = Set.copyOf(roles);
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("La transizione " + name + " deve indicare almeno un ruolo");
        }
    }

    public boolean isCreation() {
        return from.isEmpty();
    }

    public boolean allowedFrom(TicketStatus status) {
        return from.contains(status);
    }

    public boolean allowedFor(Set<Role> userRoles) {
        return userRoles.stream().anyMatch(roles::contains);
    }
}
