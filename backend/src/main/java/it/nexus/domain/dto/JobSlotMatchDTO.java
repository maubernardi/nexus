package it.nexus.domain.dto;

import java.util.List;

/**
 * Mansione proponibile (o proposta) per una segnalazione, con l'azienda che la offre e le segnalazioni di altri tipi a
 * cui è già assegnata (US-605), per l'avviso.
 */
public record JobSlotMatchDTO(
        String id,
        String title,
        String description,
        String companyId,
        String companyName,
        ReferenceItemDTO jobCategory,
        ReferenceItemDTO zone,
        long version,
        List<JobSlotAssignmentDTO> assignments) {

    public JobSlotMatchDTO withAssignments(List<JobSlotAssignmentDTO> list) {
        return new JobSlotMatchDTO(id, title, description, companyId, companyName, jobCategory, zone, version,
                List.copyOf(list));
    }
}
