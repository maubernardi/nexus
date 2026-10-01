package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.QueueItemDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.services.TicketService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/tickets")
@RequiredArgsConstructor
@Tag(name = "Segnalazioni")
public class TicketResource {

    private final TicketService ticketService;

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Coda del Call Center: prima i fast-track, poi in ordine di arrivo")
    public ResponseEntity<List<QueueItemDTO>> queue(@RequestParam(required = false) String projectId,
            @RequestParam(required = false) String zoneId) {
        return ResponseEntity.ok(ticketService.queue(projectId, zoneId));
    }

    // nessun Location: il dettaglio del ticket arriva con US-305
    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Invia una segnalazione normale per un beneficiario del Tutor corrente")
    public ResponseEntity<TicketDTO> submit(@RequestBody @Valid TicketCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.submit(dto));
    }
}
