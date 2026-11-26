package it.nexus.domain.dto;

import java.time.LocalDate;

/**
 * Valutazione di un tirocinio nella scheda dell'azienda (US-1104): data di fine, media dei tre voti, nota e nome del
 * Tutor che l'ha data (PO, 26/11). Nessun dato del beneficiario.
 */
public record CompanyReviewDTO(LocalDate endDate, double average, String note, String tutorName) {
}
