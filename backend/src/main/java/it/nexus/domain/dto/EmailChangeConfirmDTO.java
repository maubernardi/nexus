package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Codice di conferma ricevuto al nuovo indirizzo (US-110). */
public record EmailChangeConfirmDTO(
        @NotBlank(message = "Scrivi il codice ricevuto")
        @Pattern(regexp = "^\\s*$|^\\s*\\d{6}\\s*$", message = "Il codice è di 6 cifre")
        String code) {
}
