package it.nexus.domain.dto;

import java.util.List;

/** Sede di competenza con i suoi progetti (US-115). */
public record CompetenceSiteDTO(String id, String code, String name, boolean active, List<ReferenceItemDTO> projects) {
}
