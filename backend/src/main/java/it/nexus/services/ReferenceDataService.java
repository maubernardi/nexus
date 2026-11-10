package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.ReferenceItemDTO;

public interface ReferenceDataService {

    List<ReferenceItemDTO> activeZones();

    List<ReferenceItemDTO> activeJobCategories();

    /** Tipi di tirocinio attivi, per la scheda del tirocinio (US-108). */
    List<ReferenceItemDTO> activeInternshipTypes();

    /** Sedi di competenza attive di un progetto (US-308). */
    List<ReferenceItemDTO> activeCompetenceSites(String projectId);

    /** Enti di presa in carico attivi (US-206). */
    List<ReferenceItemDTO> activeCareAgencies();

    /** Formule di assunzione attive, per il report di restituzione (US-109). */
    List<ReferenceItemDTO> activeHiringFormulas();

    /** Motivi della mancata assunzione attivi, per il report di restituzione (US-109). */
    List<ReferenceItemDTO> activeNoHireReasons();

    List<ReferenceItemDTO> activeProjects();

    /** Progetti attivi a cui è assegnato l'utente corrente. */
    List<ReferenceItemDTO> myActiveProjects();
}
