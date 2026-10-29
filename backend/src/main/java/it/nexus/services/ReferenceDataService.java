package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.ReferenceItemDTO;

public interface ReferenceDataService {

    List<ReferenceItemDTO> activeZones();

    List<ReferenceItemDTO> activeJobCategories();

    /** Tipi di tirocinio attivi, per la scheda del tirocinio (US-108). */
    List<ReferenceItemDTO> activeInternshipTypes();

    /** Formule di assunzione attive, per il report di restituzione (US-109). */
    List<ReferenceItemDTO> activeHiringFormulas();

    /** Motivi della mancata assunzione attivi, per il report di restituzione (US-109). */
    List<ReferenceItemDTO> activeNoHireReasons();

    List<ReferenceItemDTO> activeProjects();

    /** Progetti attivi a cui è assegnato l'utente corrente. */
    List<ReferenceItemDTO> myActiveProjects();
}
