package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Candidatura di un beneficiario del Tutor a un annuncio della bacheca (US-306). {@code projectId} serve solo per un
 * annuncio aperto a tutti: per uno riservato vale il progetto dell'annuncio.
 */
public record TicketSpecialCreateDTO(
        @NotBlank(message = "Seleziona l'annuncio") String boardPostId,
        @NotBlank(message = "Seleziona il beneficiario") String beneficiaryId,
        String projectId) {
}
