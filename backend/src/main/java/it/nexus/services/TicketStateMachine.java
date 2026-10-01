package it.nexus.services;

import it.nexus.domain.Ticket;
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
}
