package it.nexus.domain.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Proroga dal modulo della scheda (US-1012): una convenzione con numero, data, data di stipula e date proprie, più i
 * suoi progetti formativi nell'ordine delle date. Date successive alla convenzione precedente: controllo nel servizio.
 */
public record ExtensionFormDTO(
        @NotBlank(message = "Inserisci il numero della convenzione")
        @Size(max = 50, message = "Massimo 50 caratteri") String agreementNumber,
        @NotNull(message = "Inserisci la data della convenzione") LocalDate agreementDate,
        @NotNull(message = "Inserisci la data di stipula") LocalDate agreementSignedDate,
        @NotNull(message = "Inserisci la data di inizio") LocalDate from,
        @NotNull(message = "Inserisci la data di fine") LocalDate to,
        @NotNull @Size(max = 10, message = "Massimo 10 progetti formativi")
        List<@Valid TrainingProjectFormDTO> trainingProjects) {
}
