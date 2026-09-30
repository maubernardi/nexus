package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.services.ReferenceDataService;
import it.nexus.web.rest.PathAPI;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/reference")
@RequiredArgsConstructor
@Tag(name = "Dati di riferimento")
public class ReferenceDataResource {

    private final ReferenceDataService referenceDataService;

    @GetMapping("/zones")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Zone attive")
    public ResponseEntity<List<ReferenceItemDTO>> zones() {
        return ResponseEntity.ok(referenceDataService.activeZones());
    }

    @GetMapping("/projects")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Progetti attivi (per i filtri del Call Center)")
    public ResponseEntity<List<ReferenceItemDTO>> projects() {
        return ResponseEntity.ok(referenceDataService.activeProjects());
    }

    @GetMapping("/job-categories")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Tipologie di mansione attive")
    public ResponseEntity<List<ReferenceItemDTO>> jobCategories() {
        return ResponseEntity.ok(referenceDataService.activeJobCategories());
    }
}
