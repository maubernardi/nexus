package it.nexus.web.rest.resource;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.CompanyDTO;
import it.nexus.domain.dto.CompanyFormDTO;
import it.nexus.domain.dto.VersionDTO;
import it.nexus.services.CompanyService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/companies")
@RequiredArgsConstructor
@Tag(name = "Aziende")
public class CompanyResource {

    private final CompanyService companyService;

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Cerca aziende per ragione sociale o partita IVA")
    public ResponseEntity<List<CompanyDTO>> search(@RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(companyService.search(search, includeInactive));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Dettaglio di un'azienda")
    public ResponseEntity<CompanyDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Registra un'azienda")
    public ResponseEntity<CompanyDTO> create(@RequestBody @Valid CompanyFormDTO dto) {
        CompanyDTO created = companyService.create(dto);
        return ResponseEntity.created(URI.create(PathAPI.BASE_PATH_V1 + "/companies/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Modifica un'azienda (409 se modificata nel frattempo)")
    public ResponseEntity<CompanyDTO> update(@PathVariable String id, @RequestBody @Valid CompanyFormDTO dto) {
        return ResponseEntity.ok(companyService.update(id, dto));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Disattiva un'azienda: resta nello storico ma non si propone più")
    public ResponseEntity<CompanyDTO> deactivate(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(companyService.setActive(id, body.version(), false));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Riattiva un'azienda")
    public ResponseEntity<CompanyDTO> activate(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(companyService.setActive(id, body.version(), true));
    }
}
