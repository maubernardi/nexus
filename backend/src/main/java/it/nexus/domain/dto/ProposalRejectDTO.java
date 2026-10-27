package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Rifiuto della proposta da parte del Tutor (US-702): motivo obbligatorio e versione. */
public record ProposalRejectDTO(@NotNull @PositiveOrZero Long version, @NotBlank @Size(max = 500) String reason) {
}
