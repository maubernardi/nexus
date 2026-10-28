package it.nexus.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dati modificabili di un annuncio (US-902); {@code projectId} conta solo per le bozze (null = tutti i progetti). */
public record BoardPostFormDTO(
        @NotBlank(message = "Inserisci il titolo") @Size(max = 200, message = "Massimo 200 caratteri") String title,
        String projectId,
        @Min(value = 14, message = "Minimo 14 anni") @Max(value = 99, message = "Massimo 99 anni") Integer ageMin,
        @Min(value = 14, message = "Minimo 14 anni") @Max(value = 99, message = "Massimo 99 anni") Integer ageMax,
        @Min(value = 1, message = "Minimo 1 ora") @Max(value = 60, message = "Massimo 60 ore") Integer weeklyHours,
        @Min(value = 1, message = "Minimo 1 mese") @Max(value = 36, message = "Massimo 36 mesi") Integer durationMonths,
        @Size(max = 4000, message = "Massimo 4000 caratteri") String notes,
        @NotNull(message = "Versione mancante") @PositiveOrZero Long version) {
}
