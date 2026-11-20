package it.nexus.domain.dto;

import java.util.List;

import it.nexus.domain.enumeration.JobSlotStatus;

/**
 * Mansione di un'azienda; {@code assignments} sono le segnalazioni a cui è assegnata, una per tipo di tirocinio
 * (US-605), {@code activeBoardPost*} l'annuncio attivo (bozza o pubblicato) in bacheca, se c'è (US-904).
 */
public record JobSlotDTO(
        String id,
        String companyId,
        String title,
        String description,
        ReferenceItemDTO jobCategory,
        ReferenceItemDTO zone,
        JobSlotStatus status,
        List<JobSlotAssignmentDTO> assignments,
        boolean active,
        long version,
        String activeBoardPostId,
        Long activeBoardPostNumber) {

    public JobSlotDTO withActiveBoardPost(String postId, Long postNumber) {
        return new JobSlotDTO(id, companyId, title, description, jobCategory, zone, status, assignments, active,
                version, postId, postNumber);
    }

    public JobSlotDTO withAssignments(List<JobSlotAssignmentDTO> list) {
        return new JobSlotDTO(id, companyId, title, description, jobCategory, zone, status, List.copyOf(list), active,
                version, activeBoardPostId, activeBoardPostNumber);
    }
}
