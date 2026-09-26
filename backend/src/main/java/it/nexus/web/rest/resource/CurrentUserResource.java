package it.nexus.web.rest.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.CurrentUserDTO;
import it.nexus.services.CurrentUserService;
import it.nexus.web.rest.PathAPI;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/me")
@RequiredArgsConstructor
@Tag(name = "Utente corrente")
public class CurrentUserResource {

    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Restituisce identità e ruoli dell'utente autenticato")
    public ResponseEntity<CurrentUserDTO> getCurrentUser() {
        return ResponseEntity.ok(currentUserService.getCurrentUser());
    }
}
