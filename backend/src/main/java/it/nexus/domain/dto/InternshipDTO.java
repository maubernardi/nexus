package it.nexus.domain.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheda del tirocinio per il Tutor della segnalazione (US-1001), con i dati dell'azienda che servono ai documenti
 * (US-506). {@code version} nullo = scheda mai salvata.
 */
public record InternshipDTO(
        Long version,
        String companyName,
        LegalRepresentativeDTO legalRepresentative,
        List<AtecoCodeDTO> atecoCodes,
        ReferenceItemDTO internshipType,
        LocalDate startDate,
        LocalDate expectedEndDate,
        Integer weeklyHours,
        String site,
        String atecoCodeId,
        List<CompanyTutorDTO> companyTutors,
        String fiscalCode,
        LocalDate birthDate,
        String birthPlace,
        String residence,
        List<SuspensionDTO> suspensions,
        String notes) {
}
