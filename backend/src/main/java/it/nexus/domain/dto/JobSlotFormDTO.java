package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Dati della mansione dal modulo; lo stato LIBERA/BLOCCATA non è modificabile. {@code version} solo in modifica. */
public record JobSlotFormDTO(
        @NotBlank(message = "Inserisci il titolo") @Size(max = 200, message = "Massimo 200 caratteri") String title,
        @NotBlank(message = "Seleziona la tipologia") String jobCategoryId,
        @NotBlank(message = "Seleziona la zona") String zoneId,
        @Size(max = 4000, message = "Massimo 4000 caratteri") String description,
        @PositiveOrZero Long version) {
}
