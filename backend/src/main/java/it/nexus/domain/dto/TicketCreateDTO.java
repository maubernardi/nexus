package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;

/** Segnalazione normale del Tutor: beneficiario proprio, progetto assegnato, tipologia di mansione del catalogo. */
public record TicketCreateDTO(
        @NotBlank(message = "Seleziona il beneficiario") String beneficiaryId,
        @NotBlank(message = "Seleziona il progetto") String projectId,
        @NotBlank(message = "Seleziona la mansione richiesta") String jobCategoryId) {
}
