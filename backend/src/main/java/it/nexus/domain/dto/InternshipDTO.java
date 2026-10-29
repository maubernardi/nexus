package it.nexus.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Scheda del tirocinio per il Tutor della segnalazione (US-1001), con i dati dell'azienda che servono ai documenti
 * (US-506). {@code version} nullo = scheda mai salvata. Totale settimanale e monte ore (US-1002) sono calcolati, in
 * minuti, e assenti senza griglia (il monte ore anche senza date).
 */
public record InternshipDTO(
        Long version,
        String companyName,
        LegalRepresentativeDTO legalRepresentative,
        List<AtecoCodeDTO> atecoCodes,
        ReferenceItemDTO internshipType,
        LocalDate startDate,
        LocalDate expectedEndDate,
        String site,
        String atecoCodeId,
        List<CompanyTutorDTO> companyTutors,
        String fiscalCode,
        LocalDate birthDate,
        String birthPlace,
        String residence,
        List<SuspensionDTO> suspensions,
        String notes,
        LocalTime firstSlotStart,
        LocalTime firstSlotEnd,
        LocalTime secondSlotStart,
        LocalTime secondSlotEnd,
        boolean excludeHolidays,
        List<ScheduleDayDTO> schedule,
        Integer weeklyMinutes,
        Integer totalMinutes,
        /** Dati che mancano per avviare il tirocinio (US-1003), in base alla scheda salvata; vuoto = completa. */
        List<String> missingForStart) {
}
