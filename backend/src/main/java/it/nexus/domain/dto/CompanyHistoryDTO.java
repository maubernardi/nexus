package it.nexus.domain.dto;

import java.time.Instant;
import java.util.List;

/**
 * Storico e pregressi dell'azienda (US-503): riepilogo sempre completo (voci a zero finché mancano gli eventi) ed
 * elenco dalla più recente, con le esclusioni.
 */
public record CompanyHistoryDTO(Summary summary, List<Event> events) {

    public record Summary(long proposals, long accepted, long failedAppointments, long internships, long hirings,
            long negativeOutcomes) {
    }

    /**
     * Evento dello storico. {@code type}: un {@code CompanyEventType} oppure {@code ESCLUSIONE} (azienda esclusa per una
     * segnalazione); {@code detail}: testo leggibile (mansione, esito, motivo dell'esclusione).
     */
    public record Event(Instant at, String type, String ticketId, Long ticketNumber, String detail, String authorName) {
    }
}
