package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;

/** Nuovo annuncio da una mansione libera (US-904): titolo precompilato, visibile a tutti i progetti, in bozza. */
public record BoardPostCreateDTO(@NotBlank(message = "Seleziona la mansione") String jobSlotId) {
}
