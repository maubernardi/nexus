package it.nexus.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Nuovo patrono; {@code version} per il blocco ottimistico. */
public record PatronFormDTO(
        @NotNull @Min(1) @Max(31) Integer day,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull @PositiveOrZero Long version) {
}
