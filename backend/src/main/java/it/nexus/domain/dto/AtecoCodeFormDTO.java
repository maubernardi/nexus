package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Nuovo codice ATECO (US-506): 2, 4 o 6 cifre con i punti, come nella classificazione (es. 56.10.11). */
public record AtecoCodeFormDTO(
        @NotBlank(message = "Inserisci il codice")
        @Pattern(regexp = "^\\s*[0-9]{2}(\\.[0-9]{1,2}){0,2}\\s*$", message = "Formato: 56, 56.10 oppure 56.10.11")
        String code,
        @Size(max = 200, message = "Massimo 200 caratteri") String description) {
}
