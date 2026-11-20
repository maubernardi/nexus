package it.nexus.services;

import it.nexus.domain.InternshipType;
import it.nexus.domain.Ticket;

/** Correzione del tipo di tirocinio di una segnalazione (US-605): dalla segnalazione o dalla scheda. */
public interface TicketInternshipTypeService {

    /**
     * Cambia il tipo (già controllato con {@link InternshipTypeRules}) fino all'avvio del tirocinio; se la segnalazione è
     * assegnata a una mansione, il tipo nuovo non deve averci un'altra segnalazione ({@code 400} sul campo). Dopo l'avvio
     * {@code 409}, oppure {@code 400} sul campo se {@code fromInternship}. Registra il cambio nell'audit e nella
     * cronologia. La scheda la aggiorna chi chiama.
     *
     * @return se il tipo è cambiato
     */
    boolean change(Ticket ticket, InternshipType type, boolean fromInternship);
}
