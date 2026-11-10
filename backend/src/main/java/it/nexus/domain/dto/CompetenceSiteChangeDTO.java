package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;

/** Correzione della sede di competenza di una segnalazione (US-308): solo ADMIN e Coordinatore del progetto. */
public record CompetenceSiteChangeDTO(@NotBlank(message = "Scegli la sede di competenza") String competenceSiteId) {
}
