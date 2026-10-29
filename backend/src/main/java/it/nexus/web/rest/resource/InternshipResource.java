package it.nexus.web.rest.resource;

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
import it.nexus.domain.dto.AtecoCodeDTO;
import it.nexus.domain.dto.AtecoCodeFormDTO;
import it.nexus.domain.dto.InternshipDTO;
import it.nexus.domain.dto.InternshipFormDTO;
import it.nexus.domain.dto.LegalRepresentativeDTO;
import it.nexus.domain.dto.LegalRepresentativeFormDTO;
import it.nexus.services.InternshipService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Scheda amministrativa del tirocinio (US-1001): solo il Tutor della segnalazione (controllo nel servizio). */
@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/tickets/{ticketId}/internship")
@RequiredArgsConstructor
@Tag(name = "Segnalazioni")
public class InternshipResource {

    private final InternshipService service;

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Scheda del tirocinio, con legale rappresentante e codici ATECO dell'azienda")
    public ResponseEntity<InternshipDTO> get(@PathVariable String ticketId) {
        return ResponseEntity.ok(service.get(ticketId));
    }

    @PutMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Salva la scheda del tirocinio, anche incompleta (versione nulla al primo salvataggio)")
    public ResponseEntity<InternshipDTO> save(@PathVariable String ticketId, @RequestBody @Valid InternshipFormDTO dto) {
        return ResponseEntity.ok(service.save(ticketId, dto));
    }

    @PutMapping("/legal-representative")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Inserisce il legale rappresentante dell'azienda, solo se manca")
    public ResponseEntity<LegalRepresentativeDTO> addLegalRepresentative(@PathVariable String ticketId,
            @RequestBody @Valid LegalRepresentativeFormDTO dto) {
        return ResponseEntity.ok(service.addLegalRepresentative(ticketId, dto));
    }

    @PostMapping("/ateco-codes")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Aggiunge un codice ATECO all'azienda della segnalazione")
    public ResponseEntity<AtecoCodeDTO> addAtecoCode(@PathVariable String ticketId,
            @RequestBody @Valid AtecoCodeFormDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAtecoCode(ticketId, dto));
    }
}
