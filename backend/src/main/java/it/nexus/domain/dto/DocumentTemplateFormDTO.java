package it.nexus.domain.dto;

/**
 * Condizioni di un modello caricato (US-1010), dai campi del modulo multipart: i controlli, con l'errore sul campo,
 * sono nel servizio. {@code careAgencyId} vuoto = tutti gli enti.
 */
public record DocumentTemplateFormDTO(
        String document,
        String projectId,
        String internshipTypeId,
        String careAgencyId,
        String name) {
}
