package it.nexus.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import it.nexus.domain.JobSlot;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.JobSlotAssignmentDTO;

/**
 * Assegnazioni delle mansioni alle segnalazioni (US-605): al più una per mansione e tipo di tirocinio. Tiene allineato
 * il riassunto sulla mansione (stato e segnalazione con il numero più basso) e scrive l'audit della mansione.
 */
public interface JobSlotAssignments {

    /** Le segnalazioni assegnate alla mansione, dal numero più basso. */
    List<Ticket> holders(JobSlot slot);

    /** Le assegnazioni di più mansioni, per l'elenco dell'abbinamento e delle mansioni dell'azienda. */
    Map<Long, List<JobSlotAssignmentDTO>> assignments(Collection<Long> jobSlotIds);

    /**
     * Dentro la proposta, prima di assegnare: mette in fila le proposte della stessa mansione (versione incrementata
     * subito) e rifiuta con {@code 409} se la segnalazione non ha il tipo o se la mansione ha già una segnalazione dello
     * stesso tipo o senza tipo.
     */
    void requireAssignable(Ticket ticket, JobSlot slot);

    /** Dopo un'assegnazione o un rilascio: riassunto della mansione e audit ({@code BLOCK} o {@code RELEASE}). */
    void sync(JobSlot slot, Ticket ticket, boolean assigned);

    /** «n. 12 (Extracurriculare), n. 15 (tipo non indicato)»: le segnalazioni assegnate, per i messaggi. */
    String describe(List<Ticket> holders);
}
