package it.nexus.domain.workflow;

import static it.nexus.domain.enumeration.TicketStatus.NUOVA;

import java.util.Set;

import it.nexus.domain.enumeration.Role;

/**
 * Catalogo delle transizioni del ticket (FSM del brief, fasi 1–7). Ogni storia aggiunge qui le proprie transizioni e le
 * applica tramite {@link it.nexus.services.TicketStateMachine}: le regole stanno in un solo posto.
 */
public final class TicketTransitions {

    /** Fase 1 — segnalazione normale del Tutor con mansione del catalogo (US-301). */
    public static final TicketTransition SUBMIT = new TicketTransition("SUBMIT", Set.of(), NUOVA, Set.of(Role.TUTOR));

    private TicketTransitions() {
    }
}
