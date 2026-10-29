package it.nexus.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import it.nexus.validation.FiscalCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Scheda del tirocinio dal modulo (US-1001): ogni campo è facoltativo, la completezza la verifica l'avvio (US-1003).
 * Le fasce orarie hanno sempre un valore (precompilate); la griglia ha i soli giorni con almeno una fascia (US-1002).
 * {@code version} nullo al primo salvataggio.
 */
public record InternshipFormDTO(
        String internshipTypeId,
        LocalDate startDate,
        LocalDate expectedEndDate,
        @Size(max = 300, message = "Massimo 300 caratteri") String site,
        String atecoCodeId,
        @NotNull @Size(max = 10, message = "Massimo 10 tutor aziendali") List<@Valid CompanyTutorDTO> companyTutors,
        @FiscalCode String fiscalCode,
        @Past(message = "La data di nascita deve essere passata") LocalDate birthDate,
        @Size(max = 200, message = "Massimo 200 caratteri") String birthPlace,
        @Size(max = 300, message = "Massimo 300 caratteri") String residence,
        @NotNull @Size(max = 20, message = "Massimo 20 periodi") List<@Valid SuspensionDTO> suspensions,
        @Size(max = 2000, message = "Massimo 2000 caratteri") String notes,
        @NotNull(message = "Indica l'inizio della fascia") LocalTime firstSlotStart,
        @NotNull(message = "Indica la fine della fascia") LocalTime firstSlotEnd,
        @NotNull(message = "Indica l'inizio della fascia") LocalTime secondSlotStart,
        @NotNull(message = "Indica la fine della fascia") LocalTime secondSlotEnd,
        boolean excludeHolidays,
        @NotNull @Size(max = 7) List<@Valid ScheduleDayDTO> schedule,
        @PositiveOrZero Long version) {
}
