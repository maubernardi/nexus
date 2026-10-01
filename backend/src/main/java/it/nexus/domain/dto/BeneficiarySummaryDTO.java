package it.nexus.domain.dto;

/** Riepilogo per elenchi e selezioni. */
/**
 * Beneficiario in sintesi. {@code openTicketNumber} è valorizzato solo nell'elenco del Tutor: numero della segnalazione
 * aperta, che impedisce di inviarne un'altra.
 */
public record BeneficiarySummaryDTO(String id, String firstName, String lastName, Integer birthYear, String residenceZoneName,
        Long openTicketNumber) {

    public BeneficiarySummaryDTO withOpenTicketNumber(Long number) {
        return new BeneficiarySummaryDTO(id, firstName, lastName, birthYear, residenceZoneName, number);
    }
}
