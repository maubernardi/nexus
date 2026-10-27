package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Respingimento della nuova tipologia (US-302): la tipologia corretta del catalogo e il motivo per il Tutor. */
public record RejectCategoryDTO(
        @NotBlank String jobCategoryId,
        @NotBlank @Size(max = 500) String reason,
        @NotNull @PositiveOrZero Long version) {
}
