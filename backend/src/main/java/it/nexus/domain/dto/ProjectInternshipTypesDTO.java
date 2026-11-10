package it.nexus.domain.dto;

import java.util.List;

/** Tipi di tirocinio ammessi da un progetto (US-115); vuoto = tutti i tipi. */
public record ProjectInternshipTypesDTO(ReferenceItemDTO project, boolean active,
        List<ReferenceItemDTO> internshipTypes) {
}
