package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo per cui i dati di un beneficiario si tengono oltre il periodo di conservazione (US-1303). */
public record BeneficiaryKeepDTO(
        @NotBlank(message = "Scrivi il motivo per cui i dati si tengono")
        @Size(max = 500, message = "Al massimo 500 caratteri")
        String reason) {
}
