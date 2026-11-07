package it.nexus.web.rest.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.AccessLogPageDTO;
import it.nexus.services.AccessLogService;
import it.nexus.web.rest.PathAPI;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1)
@RequiredArgsConstructor
@Tag(name = "Registro degli accessi")
public class AccessLogResource {

    private final AccessLogService accessLogService;

    @GetMapping("/users/{id}/access-log")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Accessi riusciti e falliti di un utente, dal più recente (US-113)")
    public ResponseEntity<AccessLogPageDTO> forUser(@PathVariable String id,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(accessLogService.forUser(id, page));
    }

    @GetMapping("/me/access-log")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "I propri accessi, dal più recente (US-113)")
    public ResponseEntity<AccessLogPageDTO> mine(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(accessLogService.mine(page));
    }

    @GetMapping("/access-log/failed")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Tentativi di accesso falliti di tutti, anche con uno username inesistente (US-113)")
    public ResponseEntity<AccessLogPageDTO> failed(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(accessLogService.failed(page));
    }
}
