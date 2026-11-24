package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dati corretti dall'ADMIN (US-116), con le stesse regole della creazione (US-103). */
public record UserDetailsUpdateDTO(
        @NotBlank(message = "Scrivi lo username")
        @Size(min = 3, max = 50, message = "Da 3 a 50 caratteri")
        @Pattern(regexp = "^\\s*$|^\\s*[A-Za-z0-9]+([._-][A-Za-z0-9]+)*\\s*$",
                message = "Solo lettere, cifre e i separatori . _ - (es. mario.rossi)")
        String username,
        @NotBlank(message = "Scrivi il nome") @Size(max = 100, message = "Massimo 100 caratteri") String firstName,
        @NotBlank(message = "Scrivi il cognome") @Size(max = 100, message = "Massimo 100 caratteri") String lastName,
        @NotBlank(message = "Scrivi l'indirizzo email")
        @Size(max = 254, message = "Massimo 254 caratteri")
        @Pattern(regexp = "^\\s*$|^\\s*[^@\\s]+@[^@\\s]+\\.[^@\\s]+\\s*$", message = "Indirizzo email non valido")
        String email,
        @Size(max = 30, message = "Massimo 30 caratteri")
        @Pattern(regexp = "^[0-9+ ()./-]*$", message = "Solo cifre, spazi e + ( ) . / -")
        String phone) {
}
