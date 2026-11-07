package it.nexus.domain.dto;

import java.time.Instant;

import it.nexus.domain.enumeration.RetentionStatus;

/**
 * Beneficiario nell'elenco dell'ADMIN, con la conservazione dei dati (US-1303).
 *
 * @param anonymizationDueSince da quando è da anonimizzare
 * @param keptReason            motivo per cui i dati si tengono per sempre
 */
public record BeneficiaryRetentionItemDTO(String id, String firstName, String lastName, Integer birthYear,
        String residenceZoneName, String ownerTutorName, RetentionStatus retention, Instant anonymizationDueSince,
        String keptReason) {
}
