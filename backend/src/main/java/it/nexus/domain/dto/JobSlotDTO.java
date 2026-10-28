package it.nexus.domain.dto;

import it.nexus.domain.enumeration.JobSlotStatus;

/**
 * Mansione di un'azienda; {@code blockedByTicketNumber} indica la segnalazione che la tiene bloccata,
 * {@code activeBoardPost*} l'annuncio attivo (bozza o pubblicato) in bacheca, se c'è (US-904).
 */
public record JobSlotDTO(
        String id,
        String companyId,
        String title,
        String description,
        ReferenceItemDTO jobCategory,
        ReferenceItemDTO zone,
        JobSlotStatus status,
        Long blockedByTicketNumber,
        boolean active,
        long version,
        String activeBoardPostId,
        Long activeBoardPostNumber) {

    public JobSlotDTO withActiveBoardPost(String postId, Long postNumber) {
        return new JobSlotDTO(id, companyId, title, description, jobCategory, zone, status, blockedByTicketNumber, active,
                version, postId, postNumber);
    }
}
