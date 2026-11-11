package it.nexus.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Progetto formativo dal modulo della scheda (US-1008): date, mansione e dettaglio. Date dentro il tirocinio e dopo la
 * fine del progetto precedente: controlli nel servizio.
 */
public record TrainingProjectFormDTO(
        @NotNull(message = "Inserisci la data di inizio") LocalDate from,
        @NotNull(message = "Inserisci la data di fine") LocalDate to,
        @NotBlank(message = "Inserisci la mansione") @Size(max = 200, message = "Massimo 200 caratteri") String jobTitle,
        @Size(max = 2000, message = "Massimo 2000 caratteri") String jobDetails) {
}
