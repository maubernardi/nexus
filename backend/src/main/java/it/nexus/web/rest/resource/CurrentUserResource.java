package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import it.nexus.domain.dto.CurrentUserDTO;
import it.nexus.domain.dto.EmailChangeConfirmDTO;
import it.nexus.domain.dto.EmailChangeFormDTO;
import it.nexus.domain.dto.EmailStatusDTO;
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.services.CurrentUserService;
import it.nexus.services.EmailChangeService;
import it.nexus.services.ReferenceDataService;
import it.nexus.web.rest.PathAPI;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/me")
@RequiredArgsConstructor
@Tag(name = "Utente corrente")
public class CurrentUserResource {

    private final CurrentUserService currentUserService;
    private final ReferenceDataService referenceDataService;
    private final EmailChangeService emailChangeService;

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Restituisce identità e ruoli dell'utente autenticato")
    public ResponseEntity<CurrentUserDTO> getCurrentUser() {
        return ResponseEntity.ok(currentUserService.getCurrentUser());
    }

    @GetMapping("/projects")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Progetti attivi a cui è assegnato l'utente autenticato")
    public ResponseEntity<List<ReferenceItemDTO>> getMyProjects() {
        return ResponseEntity.ok(referenceDataService.myActiveProjects());
    }

    @GetMapping("/email")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Indirizzo email registrato in NEXUS e cambio in attesa del codice (US-110)")
    public ResponseEntity<EmailStatusDTO> getMyEmail() {
        return ResponseEntity.ok(emailChangeService.status());
    }

    @PostMapping("/email-change")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Chiede il cambio del proprio indirizzo: invia un codice di 6 cifre al nuovo indirizzo")
    public ResponseEntity<EmailStatusDTO> requestEmailChange(@RequestBody @Valid EmailChangeFormDTO form) {
        return ResponseEntity.ok(emailChangeService.request(form.newEmail()));
    }

    @PostMapping("/email-change/confirm")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Conferma il cambio di indirizzo con il codice ricevuto")
    public ResponseEntity<EmailStatusDTO> confirmEmailChange(@RequestBody @Valid EmailChangeConfirmDTO form) {
        return ResponseEntity.ok(emailChangeService.confirm(form.code()));
    }

    @DeleteMapping("/email-change")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Annulla il cambio di indirizzo in corso")
    public ResponseEntity<EmailStatusDTO> cancelEmailChange() {
        return ResponseEntity.ok(emailChangeService.cancel());
    }
}
