package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.CatalogFormDTO;
import it.nexus.domain.dto.CatalogItemDTO;
import it.nexus.domain.enumeration.CatalogKind;
import it.nexus.services.CatalogService;
import it.nexus.web.errors.NotFoundException;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Cataloghi di zone ({@code zones}) e tipologie di mansione ({@code job-categories}) per l'ADMIN (US-106). */
@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/catalogs/{kind}")
@RequiredArgsConstructor
@Tag(name = "Cataloghi")
public class CatalogResource {

    private final CatalogService catalogService;

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Tutte le voci del catalogo, anche disattivate (prima le attive)")
    public ResponseEntity<List<CatalogItemDTO>> list(@PathVariable String kind) {
        return ResponseEntity.ok(catalogService.list(kind(kind)));
    }

    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Aggiunge una voce; il codice si ricava dal nome")
    public ResponseEntity<CatalogItemDTO> create(@PathVariable String kind, @RequestBody @Valid CatalogFormDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(kind(kind), dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Rinomina una voce (il codice non cambia)")
    public ResponseEntity<CatalogItemDTO> rename(@PathVariable String kind, @PathVariable String id,
            @RequestBody @Valid CatalogFormDTO dto) {
        return ResponseEntity.ok(catalogService.rename(kind(kind), id, dto));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Disattiva una voce: non più selezionabile, resta sui dati esistenti")
    public ResponseEntity<CatalogItemDTO> deactivate(@PathVariable String kind, @PathVariable String id) {
        return ResponseEntity.ok(catalogService.setActive(kind(kind), id, false));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Riattiva una voce")
    public ResponseEntity<CatalogItemDTO> activate(@PathVariable String kind, @PathVariable String id) {
        return ResponseEntity.ok(catalogService.setActive(kind(kind), id, true));
    }

    private static CatalogKind kind(String path) {
        return CatalogKind.fromPath(path).orElseThrow(() -> new NotFoundException("Catalogo non trovato"));
    }
}
