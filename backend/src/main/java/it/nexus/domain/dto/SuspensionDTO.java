package it.nexus.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/** Periodo di sospensione previsto del tirocinio (US-1001), estremi compresi. */
public record SuspensionDTO(
        @NotNull(message = "Inserisci la data di inizio") LocalDate from,
        @NotNull(message = "Inserisci la data di fine") LocalDate to) {
}
