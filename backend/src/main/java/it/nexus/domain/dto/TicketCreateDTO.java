package it.nexus.domain.dto;

import it.nexus.validation.JobChoice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Segnalazione normale del Tutor: beneficiario proprio, progetto assegnato e tipologia di mansione del catalogo
 * ({@code jobCategoryId}) oppure una nuova tipologia descritta dal Tutor ({@code jobFreeText}, US-302), mai entrambe.
 */
@JobChoice
public record TicketCreateDTO(
        @NotBlank(message = "Seleziona il beneficiario") String beneficiaryId,
        @NotBlank(message = "Seleziona il progetto") String projectId,
        String jobCategoryId,
        @Size(max = 200) String jobFreeText) {
}
