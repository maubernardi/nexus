package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.CareAgencyDTO;
import it.nexus.domain.dto.CareAgencyFormDTO;
import it.nexus.domain.dto.CompetenceSiteDTO;
import it.nexus.domain.dto.IdsFormDTO;
import it.nexus.domain.dto.ProjectInternshipTypesDTO;
import it.nexus.services.DocumentCatalogService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Dettagli dei cataloghi per i documenti reali (US-115), solo per l'ADMIN: progetti delle sedi di competenza, tipi
 * ammessi dai progetti, dati degli enti di presa in carico. Aggiunta, nome e stato: {@code /catalogs/{kind}}.
 */
@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/document-catalogs")
@RequiredArgsConstructor
@Tag(name = "Cataloghi dei documenti")
public class DocumentCatalogResource {

    private final DocumentCatalogService service;

    @GetMapping("/competence-sites")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Sedi di competenza con i loro progetti")
    public ResponseEntity<List<CompetenceSiteDTO>> competenceSites() {
        return ResponseEntity.ok(service.competenceSites());
    }

    @PutMapping("/competence-sites/{id}/projects")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Progetti di una sede di competenza")
    public ResponseEntity<CompetenceSiteDTO> setCompetenceSiteProjects(@PathVariable String id,
            @RequestBody @Valid IdsFormDTO form) {
        return ResponseEntity.ok(service.setCompetenceSiteProjects(id, form.ids()));
    }

    @GetMapping("/project-internship-types")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Tipi di tirocinio ammessi da ogni progetto (vuoto = tutti)")
    public ResponseEntity<List<ProjectInternshipTypesDTO>> projectInternshipTypes() {
        return ResponseEntity.ok(service.projectInternshipTypes());
    }

    @PutMapping("/projects/{id}/internship-types")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Tipi di tirocinio ammessi da un progetto (vuoto = tutti)")
    public ResponseEntity<ProjectInternshipTypesDTO> setProjectInternshipTypes(@PathVariable String id,
            @RequestBody @Valid IdsFormDTO form) {
        return ResponseEntity.ok(service.setProjectInternshipTypes(id, form.ids()));
    }

    @GetMapping("/care-agencies")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Enti di presa in carico con i loro dati")
    public ResponseEntity<List<CareAgencyDTO>> careAgencies() {
        return ResponseEntity.ok(service.careAgencies());
    }

    @PutMapping("/care-agencies/{id}")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Dati di un ente di presa in carico")
    public ResponseEntity<CareAgencyDTO> updateCareAgency(@PathVariable String id,
            @RequestBody @Valid CareAgencyFormDTO form) {
        return ResponseEntity.ok(service.updateCareAgency(id, form));
    }
}
