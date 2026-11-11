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
        /** Tipi che si possono scegliere: attivi e ammessi dal progetto della segnalazione, più quello scelto (US-115). */
        List<ReferenceItemDTO> internshipTypes,
        LocalDate startDate,
        LocalDate expectedEndDate,
        String site,
        MunicipalityDTO siteMunicipality,
        String atecoCodeId,
        List<CompanyTutorDTO> companyTutors,
        String fiscalCode,
        LocalDate birthDate,
        String birthPlace,
        MunicipalityDTO residenceMunicipality,
        String residenceStreet,
        String residenceStreetNumber,
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
        List<String> missingForStart,
        /**
         * Dati del beneficiario che mancano per scegliere il tipo «Di inclusione» (US-206): {@code careAgency},
         * {@code caseManagerName}, {@code caseManagerEmail}, {@code caseManagerPhone}; vuoto = si può scegliere.
         */
        List<String> inclusionMissing) {
}
