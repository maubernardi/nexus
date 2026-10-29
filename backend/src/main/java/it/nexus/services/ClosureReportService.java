package it.nexus.services;

import java.util.Optional;

import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.ClosureReportDTO;
import it.nexus.domain.dto.ClosureReportFormDTO;

/**
 * Report di restituzione (US-1101). Lo crea la transizione {@code CLOSE} del ticket, dopo i controlli di versione e
 * stato; accesso e visibilità sono quelli della segnalazione.
 */
public interface ClosureReportService {

    /**
     * Salva il report della segnalazione. Assunto: serve una formula attiva; non assunto: un motivo attivo.
     *
     * @return le scelte del report per l'audit, senza i testi liberi (possono contenere dati personali)
     * @throws it.nexus.web.errors.FieldValidationException se manca la formula o il motivo, o la voce non è attiva
     */
    AuditChanges create(Ticket ticket, ClosureReportFormDTO form);

    /** Il report inviato della segnalazione, se c'è. */
    Optional<ClosureReportDTO> find(Ticket ticket);

    /** Esito per la cronologia: «Assunto: Tempo indeterminato; mansione ancora disponibile». */
    Optional<String> outcome(Long ticketId);
}
