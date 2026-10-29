package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.AtecoCodeDTO;
import it.nexus.domain.dto.AtecoCodeFormDTO;
import it.nexus.domain.dto.LegalRepresentativeDTO;
import it.nexus.domain.dto.LegalRepresentativeFormDTO;
import it.nexus.services.CompanyLegalDataService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Legale rappresentante e codici ATECO dell'azienda (US-506), nella scheda dell'azienda. */
@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/companies/{companyId}")
@RequiredArgsConstructor
@Tag(name = "Aziende")
public class CompanyLegalDataResource {

    private final CompanyLegalDataService service;

    @GetMapping("/legal-representative")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Legale rappresentante dell'azienda (204 se non ancora indicato)")
    public ResponseEntity<LegalRepresentativeDTO> legalRepresentative(@PathVariable String companyId) {
        return service.legalRepresentative(companyId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/legal-representative")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Registra o corregge il legale rappresentante (versione nulla alla prima registrazione)")
    public ResponseEntity<LegalRepresentativeDTO> saveLegalRepresentative(@PathVariable String companyId,
            @RequestBody @Valid LegalRepresentativeFormDTO dto) {
        return ResponseEntity.ok(service.saveLegalRepresentative(companyId, dto));
    }

    @GetMapping("/ateco-codes")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Codici ATECO dell'azienda")
    public ResponseEntity<List<AtecoCodeDTO>> atecoCodes(@PathVariable String companyId) {
        return ResponseEntity.ok(service.atecoCodes(companyId));
    }

    @PostMapping("/ateco-codes")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Aggiunge un codice ATECO all'azienda")
    public ResponseEntity<AtecoCodeDTO> addAtecoCode(@PathVariable String companyId,
            @RequestBody @Valid AtecoCodeFormDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAtecoCode(companyId, dto));
    }

    @DeleteMapping("/ateco-codes/{atecoCodeId}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Toglie un codice ATECO dall'azienda")
    public ResponseEntity<Void> removeAtecoCode(@PathVariable String companyId, @PathVariable String atecoCodeId) {
        service.removeAtecoCode(companyId, atecoCodeId);
        return ResponseEntity.noContent().build();
    }
}
