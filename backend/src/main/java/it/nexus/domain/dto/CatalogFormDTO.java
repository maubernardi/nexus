package it.nexus.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Nome di una voce di catalogo (US-106); il codice si ricava dal nome alla creazione e poi non cambia. */
public record CatalogFormDTO(
        @NotBlank(message = "Inserisci il nome") @Size(max = 200, message = "Massimo 200 caratteri") String name) {
}
