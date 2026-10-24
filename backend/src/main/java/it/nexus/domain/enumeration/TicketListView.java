package it.nexus.domain.enumeration;

import java.util.Set;

/** Viste dell'elenco delle segnalazioni (Home, US-303) e ruoli che possono usarle. */
public enum TicketListView {
    /** Tutor: le proprie segnalazioni. */
    MINE(Set.of(Role.TUTOR)),
    /** Tutor: le segnalazioni dei progetti a cui è assegnato, anche dei colleghi (Q11). */
    PROJECT(Set.of(Role.TUTOR)),
    /** Call Center: la coda (nuove e speciali non ancora prese in carico), fast-track in cima, poi per arrivo. */
    TO_TAKE(Set.of(Role.CALL_CENTER, Role.ADMIN)),
    /** Call Center: le segnalazioni aperte in carico all'operatore corrente. */
    ASSIGNED_TO_ME(Set.of(Role.CALL_CENTER, Role.ADMIN)),
    /** Call Center: tutte le segnalazioni. */
    ALL(Set.of(Role.CALL_CENTER, Role.ADMIN));

    private final Set<Role> roles;

    TicketListView(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean allowedFor(Set<Role> userRoles) {
        return userRoles.stream().anyMatch(roles::contains);
    }
}
