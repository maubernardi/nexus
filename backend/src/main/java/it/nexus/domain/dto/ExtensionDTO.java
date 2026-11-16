package it.nexus.domain.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Proroga salvata (US-1012): il numero è la posizione nell'ordine delle date; i progetti formativi sono numerati al suo
 * interno, con mesi e monte ore calcolati sulle loro date.
 */
public record ExtensionDTO(
        int number,
        String agreementNumber,
        LocalDate agreementDate,
        LocalDate agreementSignedDate,
        LocalDate from,
        LocalDate to,
        List<TrainingProjectDTO> trainingProjects) {
}
