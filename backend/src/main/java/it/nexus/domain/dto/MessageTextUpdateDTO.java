package it.nexus.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nuovo testo di una parte di un messaggio (US-1204). */
public record MessageTextUpdateDTO(
        @NotNull(message = "Scrivi il testo") @Size(max = 4000, message = "Massimo 4000 caratteri") String text) {
}
