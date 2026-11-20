package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;

/** Tipo di tirocinio indicato o corretto dal Tutor della segnalazione, fino all'avvio (US-605). */
public record InternshipTypeChangeDTO(@NotBlank(message = "Scegli il tipo di tirocinio") String internshipTypeId) {
}
