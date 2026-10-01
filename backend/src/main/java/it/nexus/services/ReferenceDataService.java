package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.ReferenceItemDTO;

public interface ReferenceDataService {

    List<ReferenceItemDTO> activeZones();

    List<ReferenceItemDTO> activeJobCategories();

    List<ReferenceItemDTO> activeProjects();

    /** Progetti attivi a cui è assegnato l'utente corrente. */
    List<ReferenceItemDTO> myActiveProjects();
}
