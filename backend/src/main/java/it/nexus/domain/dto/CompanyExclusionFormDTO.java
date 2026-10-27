package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Esclusione di un'azienda per la segnalazione (US-603): azienda e motivo obbligatorio. */
public record CompanyExclusionFormDTO(@NotBlank String companyId, @NotBlank @Size(max = 500) String reason) {
}
