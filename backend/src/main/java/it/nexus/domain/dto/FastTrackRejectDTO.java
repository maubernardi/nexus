package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Rifiuto della corsia veloce (US-404): motivo obbligatorio (EN-2) e versione vista dal client. */
public record FastTrackRejectDTO(
        @NotNull(message = "Versione mancante") @PositiveOrZero Long version,
        @NotBlank(message = "Scrivi il motivo") @Size(max = 500, message = "Massimo 500 caratteri") String reason) {
}
