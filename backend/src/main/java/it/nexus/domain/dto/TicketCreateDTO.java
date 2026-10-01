package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;

/** Segnalazione normale del Tutor: candidato proprio, progetto assegnato, tipologia di mansione del catalogo. */
public record TicketCreateDTO(
        @NotBlank(message = "Seleziona il candidato") String candidateId,
        @NotBlank(message = "Seleziona il progetto") String projectId,
        @NotBlank(message = "Seleziona la mansione richiesta") String jobCategoryId) {
}
