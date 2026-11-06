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
import it.nexus.domain.dto.UserCreateDTO;
import it.nexus.domain.dto.UserCreatedDTO;
import it.nexus.domain.dto.UserActiveDTO;
import it.nexus.domain.dto.UserDTO;
import it.nexus.domain.dto.UserImpactDTO;
import it.nexus.domain.dto.UserProjectsDTO;
import it.nexus.domain.dto.UserRoleDTO;
import it.nexus.services.UserAdminService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/users")
@RequiredArgsConstructor
@Tag(name = "Utenti")
public class UserResource {

    private final UserAdminService userAdminService;

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Utenti di NEXUS, prima gli attivi (US-103)")
    public ResponseEntity<List<UserDTO>> list() {
        return ResponseEntity.ok(userAdminService.list());
    }

    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Crea l'utente in NEXUS e in Keycloak e gli invia l'email per impostare la password")
    public ResponseEntity<UserCreatedDTO> create(@RequestBody @Valid UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.create(dto));
    }

    @PostMapping("/{id}/invitation")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Invia di nuovo l'email per impostare la password")
    public ResponseEntity<Void> resendInvitation(@PathVariable String id) {
        userAdminService.resendInvitation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Un utente con i progetti assegnati (US-104)")
    public ResponseEntity<UserDTO> get(@PathVariable String id) {
        return ResponseEntity.ok(userAdminService.get(id));
    }

    @PutMapping("/{id}/projects")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Sostituisce i progetti assegnati a un Tutor (US-104)")
    public ResponseEntity<UserDTO> updateProjects(@PathVariable String id, @RequestBody @Valid UserProjectsDTO dto) {
        return ResponseEntity.ok(userAdminService.updateProjects(id, dto));
    }

    @GetMapping("/{id}/impact")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Segnalazioni aperte e ticket in carico: cosa tocca disattivare l'utente o cambiarne il ruolo")
    public ResponseEntity<UserImpactDTO> impact(@PathVariable String id) {
        return ResponseEntity.ok(userAdminService.impact(id));
    }

    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Disattiva o riattiva l'utente in NEXUS e in Keycloak (US-105)")
    public ResponseEntity<UserDTO> setActive(@PathVariable String id, @RequestBody @Valid UserActiveDTO dto) {
        return ResponseEntity.ok(userAdminService.setActive(id, dto.active()));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Cambia il ruolo in NEXUS e in Keycloak; vale dal login successivo (US-105)")
    public ResponseEntity<UserDTO> changeRole(@PathVariable String id, @RequestBody @Valid UserRoleDTO dto) {
        return ResponseEntity.ok(userAdminService.changeRole(id, dto.role()));
    }
}
