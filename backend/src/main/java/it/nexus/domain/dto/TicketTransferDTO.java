package it.nexus.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Segnalazioni aperte di un progetto da spostare sul Tutor indicato (US-111). */
public record TicketTransferDTO(
        @NotEmpty(message = "Scegli almeno una segnalazione")
        @Size(max = 500, message = "Troppe segnalazioni in una volta")
        List<@NotBlank(message = "Segnalazione non valida") String> ticketIds,
        @NotBlank(message = "Scegli il Tutor che riceve le segnalazioni") String tutorId) {
}
