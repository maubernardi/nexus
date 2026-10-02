package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Abbinamento di una mansione: versioni viste di ticket e mansione, per rifiutare le azioni su dati superati. */
public record MatchRequestDTO(
        @NotBlank(message = "Seleziona la mansione") String jobSlotId,
        @NotNull @PositiveOrZero Long ticketVersion,
        @NotNull @PositiveOrZero Long jobSlotVersion) {
}
