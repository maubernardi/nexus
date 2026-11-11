package it.nexus.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Disattiva o riattiva un modello (US-1010), con la versione letta. */
public record DocumentTemplateActiveDTO(
        @NotNull(message = "Indica se il modello è attivo") Boolean active,
        @NotNull(message = "Versione mancante") @PositiveOrZero Long version) {
}
