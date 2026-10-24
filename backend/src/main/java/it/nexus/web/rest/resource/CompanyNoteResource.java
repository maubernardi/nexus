package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import it.nexus.domain.dto.CompanyNoteDTO;
import it.nexus.domain.dto.CompanyNoteFormDTO;
import it.nexus.services.CompanyNoteService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/companies/{companyId}/notes")
@RequiredArgsConstructor
@Tag(name = "Aziende")
public class CompanyNoteResource {

    private final CompanyNoteService noteService;

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Diario delle note dell'azienda, dalla più recente")
    public ResponseEntity<List<CompanyNoteDTO>> list(@PathVariable String companyId) {
        return ResponseEntity.ok(noteService.list(companyId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Aggiunge una nota al diario dell'azienda (le note non si modificano né si cancellano)")
    public ResponseEntity<CompanyNoteDTO> add(@PathVariable String companyId, @RequestBody @Valid CompanyNoteFormDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.add(companyId, dto));
    }
}
