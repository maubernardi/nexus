package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Tutor aziendale del tirocinio (US-1001): nome obbligatorio; funzione o ruolo in azienda (US-1009) e recapito (telefono
 * o email) facoltativi.
 */
public record CompanyTutorDTO(
        @NotBlank(message = "Inserisci il nome") @Size(max = 200, message = "Massimo 200 caratteri") String fullName,
        @Size(max = 200, message = "Massimo 200 caratteri") String role,
        @Size(max = 200, message = "Massimo 200 caratteri") String contact) {
}
