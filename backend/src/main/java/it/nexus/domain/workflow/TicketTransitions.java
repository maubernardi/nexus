package it.nexus.domain.workflow;

import static it.nexus.domain.enumeration.TicketStatus.APPUNTAMENTO;
import static it.nexus.domain.enumeration.TicketStatus.IN_ATTESA_APPROVAZIONE;
import static it.nexus.domain.enumeration.TicketStatus.IN_LAVORAZIONE;
import static it.nexus.domain.enumeration.TicketStatus.NUOVA;
import static it.nexus.domain.enumeration.TicketStatus.PROPOSTA_ACCOLTA;
import static it.nexus.domain.enumeration.TicketStatus.PROPOSTA_AZIENDA;

import java.util.Set;

import it.nexus.domain.enumeration.Role;

/**
 * Catalogo delle transizioni del ticket (FSM del brief, fasi 1–7). Ogni storia aggiunge qui le proprie transizioni e le
 * applica tramite {@link it.nexus.services.TicketStateMachine}: le regole stanno in un solo posto.
 */
public final class TicketTransitions {

    /** Fase 1 — segnalazione normale del Tutor con mansione del catalogo (US-301). */
    public static final TicketTransition SUBMIT = new TicketTransition("SUBMIT", Set.of(), NUOVA, Set.of(Role.TUTOR));

    /**
     * Fase 1 — segnalazione con una nuova tipologia di mansione scritta dal Tutor (US-302): attende l'approvazione del
     * Call Center. Stesso nome di {@link #SUBMIT}: in cronologia è comunque "Segnalazione inviata".
     */
    public static final TicketTransition SUBMIT_NEW_CATEGORY = new TicketTransition("SUBMIT", Set.of(),
            IN_ATTESA_APPROVAZIONE, Set.of(Role.TUTOR));

    /** Fase 1 — un operatore approva la nuova tipologia, che entra nel catalogo; la segnalazione va in coda (US-302). */
    public static final TicketTransition APPROVE_CATEGORY = new TicketTransition("CATEGORY_APPROVE",
            Set.of(IN_ATTESA_APPROVAZIONE), NUOVA, Set.of(Role.CALL_CENTER, Role.ADMIN));

    /**
     * Fase 1 — un operatore respinge la nuova tipologia e sceglie quella corretta del catalogo, con il motivo; la
     * segnalazione va in coda e il Tutor ne è avvisato (US-302).
     */
    public static final TicketTransition REJECT_CATEGORY = new TicketTransition("CATEGORY_REJECT",
            Set.of(IN_ATTESA_APPROVAZIONE), NUOVA, Set.of(Role.CALL_CENTER, Role.ADMIN));

    /**
     * Fase 3 — presa in carico dalla coda (US-402): una segnalazione nuova, o speciale già in lavorazione ma non
     * assegnata, passa all'operatore che la prende.
     */
    public static final TicketTransition TAKE_CHARGE = new TicketTransition("TAKE_CHARGE", Set.of(NUOVA, IN_LAVORAZIONE),
            IN_LAVORAZIONE, Set.of(Role.CALL_CENTER, Role.ADMIN));

    /** Fase 4 — abbinamento a una mansione libera e proposta all'azienda (US-602). */
    public static final TicketTransition MATCH = new TicketTransition("MATCH", Set.of(IN_LAVORAZIONE), PROPOSTA_AZIENDA,
            Set.of(Role.CALL_CENTER, Role.ADMIN));

    /**
     * Fase 5 — il Tutor accetta la proposta (US-701): parte il timer di 7 giorni lavorativi. Solo il Tutor della
     * segnalazione (controllo nel servizio).
     */
    public static final TicketTransition ACCEPT = new TicketTransition("ACCEPT", Set.of(PROPOSTA_AZIENDA),
            PROPOSTA_ACCOLTA, Set.of(Role.TUTOR));

    /**
     * Fase 6 — il Tutor registra che l'appuntamento con l'azienda è fissato (US-801): il timer si ferma. Solo il Tutor
     * della segnalazione (controllo nel servizio).
     */
    public static final TicketTransition APPOINTMENT_SET = new TicketTransition("APPOINTMENT_SET",
            Set.of(PROPOSTA_ACCOLTA), APPUNTAMENTO, Set.of(Role.TUTOR));

    private TicketTransitions() {
    }
}
