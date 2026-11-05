package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Nuovo indirizzo email richiesto dall'utente (US-110). */
public record EmailChangeFormDTO(
        @NotBlank(message = "Scrivi il nuovo indirizzo email")
        @Size(max = 254, message = "Massimo 254 caratteri")
        @Pattern(regexp = "^\\s*$|^\\s*[^@\\s]+@[^@\\s]+\\.[^@\\s]+\\s*$", message = "Indirizzo email non valido")
        String newEmail) {
}
