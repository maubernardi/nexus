package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Variabile libera di un progetto (US-1013): il nome è il segnaposto {@code {NOME}} nei modelli del progetto.
 */
public record ProjectVariableDTO(
        @NotBlank(message = "Inserisci il nome")
        @Pattern(regexp = "^[A-Z][A-Z0-9_]{0,49}$",
                message = "Lettere maiuscole, cifre e trattino basso, cominciando con una lettera (massimo 50)")
        String name,
        @NotBlank(message = "Inserisci il valore") @Size(max = 500, message = "Massimo 500 caratteri") String value,
        @Size(max = 300, message = "Massimo 300 caratteri") String description) {
}
