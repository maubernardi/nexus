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
import it.nexus.domain.dto.ContactRequestDTO;
import it.nexus.domain.dto.ContactRequestFormDTO;
import it.nexus.domain.dto.VersionDTO;
import it.nexus.services.ContactRequestService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Richieste di contatto di un'azienda dal Tutor al Call Center (US-507). */
@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/contact-requests")
@RequiredArgsConstructor
@Tag(name = "Richieste di contatto")
public class ContactRequestResource {

    private final ContactRequestService service;

    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Il Tutor chiede di contattare un'azienda: già presente (companyId) o nuova (nome e zona)")
    public ResponseEntity<ContactRequestDTO> create(@RequestBody @Valid ContactRequestFormDTO form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(form));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Le richieste del Tutor corrente, dalla più recente")
    public ResponseEntity<List<ContactRequestDTO>> mine() {
        return ResponseEntity.ok(service.mine());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Richieste aperte e in carico, dalla più vecchia")
    public ResponseEntity<List<ContactRequestDTO>> queue() {
        return ResponseEntity.ok(service.queue());
    }

    @GetMapping("/open-count")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Numero delle richieste ancora da prendere in carico, per il menu")
    public ResponseEntity<Long> openCount() {
        return ResponseEntity.ok(service.openCount());
    }

    @PostMapping("/{id}/take")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Prende in carico una richiesta aperta (409 se già presa o cambiata)")
    public ResponseEntity<ContactRequestDTO> take(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(service.take(id, body.version()));
    }
}
