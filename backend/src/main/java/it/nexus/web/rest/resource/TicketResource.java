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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.JobSlotMatchDTO;
import it.nexus.domain.dto.MatchRequestDTO;
import it.nexus.domain.dto.QueueItemDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.dto.TicketDetailDTO;
import it.nexus.domain.dto.VersionDTO;
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

    @PostMapping("/{id}/take-charge")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Prende in carico una segnalazione dalla coda (409 se già presa o modificata)")
    public ResponseEntity<QueueItemDTO> takeCharge(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(ticketService.takeCharge(id, body.version()));
    }

    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Segnalazioni aperte assegnate all'operatore corrente")
    public ResponseEntity<List<QueueItemDTO>> assignedToMe() {
        return ResponseEntity.ok(ticketService.assignedToMe());
    }

    @GetMapping("/{id}/work")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Dettaglio della segnalazione per la lavorazione del Call Center")
    public ResponseEntity<TicketDetailDTO> getForWork(@PathVariable String id) {
        return ResponseEntity.ok(ticketService.getForWork(id));
    }

    @GetMapping("/{id}/compatible-job-slots")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Mansioni libere proponibili per la segnalazione, con filtri per zona e tipologia")
    public ResponseEntity<List<JobSlotMatchDTO>> compatibleJobSlots(@PathVariable String id,
            @RequestParam(required = false) String zoneId, @RequestParam(required = false) String jobCategoryId) {
        return ResponseEntity.ok(ticketService.compatibleJobSlots(id, zoneId, jobCategoryId));
    }

    @PostMapping("/{id}/match")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Abbina una mansione e propone il beneficiario all'azienda (409 se la mansione non è più libera)")
    public ResponseEntity<TicketDetailDTO> match(@PathVariable String id, @RequestBody @Valid MatchRequestDTO body) {
        return ResponseEntity.ok(ticketService.match(id, body));
    }

    // nessun Location: il dettaglio del ticket arriva con US-305
    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Invia una segnalazione normale per un beneficiario del Tutor corrente")
    public ResponseEntity<TicketDTO> submit(@RequestBody @Valid TicketCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.submit(dto));
    }
}
