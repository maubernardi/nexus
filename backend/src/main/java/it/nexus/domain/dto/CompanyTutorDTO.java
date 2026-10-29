package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Tutor aziendale del tirocinio (US-1001): nome obbligatorio, recapito (telefono o email) facoltativo. */
public record CompanyTutorDTO(
        @NotBlank(message = "Inserisci il nome") @Size(max = 200, message = "Massimo 200 caratteri") String fullName,
        @Size(max = 200, message = "Massimo 200 caratteri") String contact) {
}
