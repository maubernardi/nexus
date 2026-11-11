package it.nexus.domain.dto;

import java.time.Instant;

/**
 * Modello Word caricato dall'ADMIN (US-1010): documento ({@code CONVENZIONE}, {@code PROGETTO_FORMATIVO},
 * {@code RELAZIONE_FINALE}), condizioni, file e stato. {@code careAgency} assente = vale per tutti gli enti.
 */
public record DocumentTemplateDTO(
        String id,
        String document,
        ReferenceItemDTO project,
        ReferenceItemDTO internshipType,
        ReferenceItemDTO careAgency,
        String name,
        String fileName,
        long sizeBytes,
        boolean active,
        Instant uploadedAt,
        Instant updatedAt,
        long version) {
}
