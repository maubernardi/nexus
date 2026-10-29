package it.nexus.domain.dto;

import java.time.LocalDate;
import java.util.List;

import it.nexus.validation.FiscalCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Scheda del tirocinio dal modulo (US-1001): ogni campo è facoltativo, la completezza la verifica l'avvio (US-1003).
 * {@code version} nullo al primo salvataggio.
 */
public record InternshipFormDTO(
        String internshipTypeId,
        LocalDate startDate,
        LocalDate expectedEndDate,
        @Min(value = 1, message = "Minimo 1 ora") @Max(value = 40, message = "Massimo 40 ore") Integer weeklyHours,
        @Size(max = 300, message = "Massimo 300 caratteri") String site,
        String atecoCodeId,
        @NotNull @Size(max = 10, message = "Massimo 10 tutor aziendali") List<@Valid CompanyTutorDTO> companyTutors,
        @FiscalCode String fiscalCode,
        @Past(message = "La data di nascita deve essere passata") LocalDate birthDate,
        @Size(max = 200, message = "Massimo 200 caratteri") String birthPlace,
        @Size(max = 300, message = "Massimo 300 caratteri") String residence,
        @NotNull @Size(max = 20, message = "Massimo 20 periodi") List<@Valid SuspensionDTO> suspensions,
        @Size(max = 2000, message = "Massimo 2000 caratteri") String notes,
        @PositiveOrZero Long version) {
}
