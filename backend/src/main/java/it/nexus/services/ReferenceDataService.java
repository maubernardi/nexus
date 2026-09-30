package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.ReferenceItemDTO;

public interface ReferenceDataService {

    List<ReferenceItemDTO> activeZones();

    List<ReferenceItemDTO> activeJobCategories();
}
