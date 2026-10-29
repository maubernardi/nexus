package it.nexus.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Giorno della griglia oraria (US-1002): 1 = lunedì … 7 = domenica, fasce scelte e minuti del giorno, nulli = fasce
 * intere.
 */
public record ScheduleDayDTO(
        @NotNull @Min(1) @Max(7) Integer dayOfWeek,
        boolean firstSlot,
        boolean secondSlot,
        @Positive(message = "Le ore devono essere più di zero") Integer minutes) {
}
