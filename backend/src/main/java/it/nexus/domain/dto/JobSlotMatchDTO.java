package it.nexus.domain.dto;

/** Mansione proponibile (o proposta) per una segnalazione, con l'azienda che la offre. */
public record JobSlotMatchDTO(
        String id,
        String title,
        String description,
        String companyId,
        String companyName,
        ReferenceItemDTO jobCategory,
        ReferenceItemDTO zone,
        long version) {
}
