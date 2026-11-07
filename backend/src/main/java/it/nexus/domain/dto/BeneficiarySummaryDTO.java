package it.nexus.domain.dto;

import it.nexus.domain.enumeration.RetentionStatus;

/** Riepilogo per elenchi e selezioni. */
/**
 * Beneficiario in sintesi. {@code openTicketNumber} è valorizzato solo nell'elenco del Tutor: numero della segnalazione
 * aperta, che impedisce di inviarne un'altra. {@code retention}: conservazione dei dati, assente se normale (US-1303).
 */
public record BeneficiarySummaryDTO(String id, String firstName, String lastName, Integer birthYear, String residenceZoneName,
        Long openTicketNumber, RetentionStatus retention) {

    public BeneficiarySummaryDTO withOpenTicketNumber(Long number) {
        return new BeneficiarySummaryDTO(id, firstName, lastName, birthYear, residenceZoneName, number, retention);
    }
}
