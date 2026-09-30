package it.nexus.web.rest.resource;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.CandidateCreateDTO;
import it.nexus.domain.dto.CandidateDTO;
import it.nexus.domain.dto.CandidateSummaryDTO;
import it.nexus.services.CandidateService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidati")
public class CandidateResource {

    private final CandidateService candidateService;

    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Registra un candidato del Tutor corrente")
    public ResponseEntity<CandidateDTO> register(@RequestBody @Valid CandidateCreateDTO dto) {
        CandidateDTO created = candidateService.register(dto);
        return ResponseEntity.created(URI.create(PathAPI.BASE_PATH_V1 + "/candidates/" + created.id())).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Candidati di cui il Tutor corrente è proprietario")
    public ResponseEntity<List<CandidateSummaryDTO>> listMine() {
        return ResponseEntity.ok(candidateService.listMine());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Dettaglio di un candidato (proprietario, Call Center, ADMIN)")
    public ResponseEntity<CandidateDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(candidateService.getById(id));
    }
}
