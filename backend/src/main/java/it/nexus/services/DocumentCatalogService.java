package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.CareAgencyDTO;
import it.nexus.domain.dto.CareAgencyFormDTO;
import it.nexus.domain.dto.CompetenceSiteDTO;
import it.nexus.domain.dto.ProjectInternshipTypesDTO;

/**
 * Dettagli dei cataloghi per i documenti reali (US-115): progetti delle sedi di competenza, tipi di tirocinio ammessi
 * dai progetti, dati degli enti di presa in carico. Aggiunta, nome e stato passano da {@link CatalogService}.
 */
public interface DocumentCatalogService {

    List<CompetenceSiteDTO> competenceSites();

    /** Progetti di una sede: si aggiungono solo progetti attivi; quelli già collegati restano anche se disattivati. */
    CompetenceSiteDTO setCompetenceSiteProjects(String siteId, List<String> projectIds);

    List<ProjectInternshipTypesDTO> projectInternshipTypes();

    /** Tipi ammessi da un progetto; vuoto = tutti. Si aggiungono solo tipi attivi. */
    ProjectInternshipTypesDTO setProjectInternshipTypes(String projectId, List<String> internshipTypeIds);

    List<CareAgencyDTO> careAgencies();

    CareAgencyDTO updateCareAgency(String id, CareAgencyFormDTO form);
}
