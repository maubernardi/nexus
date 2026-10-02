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
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.JobSlotDTO;
import it.nexus.domain.dto.JobSlotFormDTO;
import it.nexus.domain.dto.VersionDTO;
import it.nexus.services.JobSlotService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Mansioni")
public class JobSlotResource {

    private final JobSlotService jobSlotService;

    @GetMapping(PathAPI.BASE_PATH_V1 + "/companies/{companyId}/job-slots")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Mansioni di un'azienda")
    public ResponseEntity<List<JobSlotDTO>> list(@PathVariable String companyId) {
        return ResponseEntity.ok(jobSlotService.listByCompany(companyId));
    }

    @PostMapping(PathAPI.BASE_PATH_V1 + "/companies/{companyId}/job-slots")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Aggiunge una mansione libera a un'azienda attiva")
    public ResponseEntity<JobSlotDTO> create(@PathVariable String companyId, @RequestBody @Valid JobSlotFormDTO dto) {
        JobSlotDTO created = jobSlotService.create(companyId, dto);
        return ResponseEntity.created(URI.create(PathAPI.BASE_PATH_V1 + "/job-slots/" + created.id())).body(created);
    }

    @PutMapping(PathAPI.BASE_PATH_V1 + "/job-slots/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Modifica titolo, tipologia, zona e descrizione (lo stato non si modifica a mano)")
    public ResponseEntity<JobSlotDTO> update(@PathVariable String id, @RequestBody @Valid JobSlotFormDTO dto) {
        return ResponseEntity.ok(jobSlotService.update(id, dto));
    }

    @PostMapping(PathAPI.BASE_PATH_V1 + "/job-slots/{id}/deactivate")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Ritira una mansione libera")
    public ResponseEntity<JobSlotDTO> deactivate(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(jobSlotService.setActive(id, body.version(), false));
    }

    @PostMapping(PathAPI.BASE_PATH_V1 + "/job-slots/{id}/activate")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Rimette a disposizione una mansione ritirata")
    public ResponseEntity<JobSlotDTO> activate(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(jobSlotService.setActive(id, body.version(), true));
    }
}
