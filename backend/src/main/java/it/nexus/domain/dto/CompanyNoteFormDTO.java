package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Nuova nota del diario dell'azienda. */
public record CompanyNoteFormDTO(@NotBlank @Size(max = 2000) String text) {
}
