package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Approvazione della nuova tipologia (US-302): il nome, eventualmente corretto dall'operatore. */
public record ApproveCategoryDTO(@NotBlank @Size(max = 200) String name, @NotNull @PositiveOrZero Long version) {
}
