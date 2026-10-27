package it.nexus.domain.workflow;

import java.util.Set;

import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;

/**
 * Transizione ammessa della macchina a stati del ticket: da quali stati parte, in quale arriva e quali ruoli possono
 * richiederla. Con {@code from} vuoto è una transizione di creazione (stato iniziale del ticket). Le transizioni
 * {@code bySystem} (es. scadenza del timer) non hanno ruoli: le applica solo un'attività pianificata, mai un utente.
 */
public record TicketTransition(String name, Set<TicketStatus> from, TicketStatus to, Set<Role> roles,
        boolean bySystem) {

    public TicketTransition {
        from = Set.copyOf(from);
        roles = Set.copyOf(roles);
        if (bySystem != roles.isEmpty()) {
            throw new IllegalArgumentException("La transizione " + name
                    + (bySystem ? " del sistema non ammette ruoli" : " deve indicare almeno un ruolo"));
        }
    }

    /** Transizione richiesta da un utente con uno dei ruoli indicati. */
    public TicketTransition(String name, Set<TicketStatus> from, TicketStatus to, Set<Role> roles) {
        this(name, from, to, roles, false);
    }

    /** Transizione applicata solo dal sistema (attività pianificate). */
    public static TicketTransition bySystem(String name, Set<TicketStatus> from, TicketStatus to) {
        return new TicketTransition(name, from, to, Set.of(), true);
    }

    public boolean isCreation() {
        return from.isEmpty();
    }

    public boolean allowedFrom(TicketStatus status) {
        return from.contains(status);
    }

    public boolean allowedFor(Set<Role> userRoles) {
        return !bySystem && userRoles.stream().anyMatch(roles::contains);
    }
}
