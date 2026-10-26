package it.nexus.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nuova chiusura: un giorno ({@code endDate} assente o uguale) o un periodo. */
public record ClosureFormDTO(
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotBlank @Size(max = 200) String description) {
}
