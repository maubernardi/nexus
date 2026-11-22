package it.nexus.domain.dto;

import java.time.LocalDate;

/**
 * Tirocinio finito alla data prevista e non ancora chiuso (US-1203): finché il Tutor ne ha, non apre nuove
 * segnalazioni. Solo il numero della segnalazione e la data: nessun dato del beneficiario.
 *
 * @param ticketId segnalazione del tirocinio
 * @param endDate fine prevista, con le proroghe
 */
public record InternshipToCloseDTO(String ticketId, Long ticketNumber, LocalDate endDate) {
}
