package it.nexus.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Progetti di un Tutor scelti dall'ADMIN (US-104): sostituiscono quelli assegnati. Vuoto = nessun progetto. Tra questi,
 * quelli che il Tutor coordina (US-112); assente = nessuno.
 */
public record UserProjectsDTO(
        @NotNull(message = "Indica i progetti")
        @Size(max = 200, message = "Troppi progetti")
        List<@NotBlank(message = "Progetto non valido") String> projectIds,
        @Size(max = 200, message = "Troppi progetti")
        List<@NotBlank(message = "Progetto non valido") String> coordinatedProjectIds) {
}
