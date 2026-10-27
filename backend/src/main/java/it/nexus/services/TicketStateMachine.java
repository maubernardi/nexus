package it.nexus.services;

import java.util.function.Function;

import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.workflow.TicketTransition;

/** Unico punto in cui cambia lo stato di un ticket: verifica le regole e registra la cronologia. */
public interface TicketStateMachine {

    /**
     * Salva un nuovo ticket nello stato iniziale della transizione di creazione indicata.
     *
     * @throws org.springframework.security.access.AccessDeniedException se il ruolo dell'utente non è ammesso
     */
    Ticket create(Ticket ticket, TicketTransition transition, String note);

    /**
     * Applica una transizione a un ticket esistente partendo dalla versione vista dal client.
     *
     * @throws org.springframework.security.access.AccessDeniedException se il ruolo dell'utente non è ammesso
     * @throws it.nexus.web.errors.ConflictException se la versione è superata o la transizione non parte dallo stato corrente
     */
    Ticket apply(Ticket ticket, TicketTransition transition, long expectedVersion, String note);

    /**
     * Come {@link #apply(Ticket, TicketTransition, long, String)}, con effetti propri della transizione (es. assegnazione
     * dell'operatore): {@code effects} è invocato solo dopo i controlli, modifica il ticket e restituisce le modifiche da
     * registrare nell'audit insieme al cambio di stato.
     */
    /**
     * Applica una transizione del sistema ({@link TicketTransition#bySystem()}): solo da un'attività pianificata, senza
     * utente collegato, partendo dallo stato attuale del ticket.
     *
     * @throws IllegalStateException se la transizione non è del sistema o c'è un utente collegato
     * @throws it.nexus.web.errors.ConflictException se la transizione non parte dallo stato corrente
     */
    Ticket applyBySystem(Ticket ticket, TicketTransition transition, Function<Ticket, AuditChanges> effects);

    Ticket apply(Ticket ticket, TicketTransition transition, long expectedVersion, String note,
            Function<Ticket, AuditChanges> effects);
}
