package it.nexus.web.rest.resource;

import java.util.List;

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
import it.nexus.domain.dto.TicketTransferDTO;
import it.nexus.domain.dto.TicketTransferResultDTO;
import it.nexus.domain.dto.TransferGroupDTO;
import it.nexus.services.TicketTransferService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1)
@RequiredArgsConstructor
@Tag(name = "Spostamento delle segnalazioni")
public class TicketTransferResource {

    private final TicketTransferService ticketTransferService;

    @GetMapping("/users/{id}/open-tickets")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Segnalazioni aperte di un Tutor per progetto, con i Tutor che possono riceverle (US-111)")
    public ResponseEntity<List<TransferGroupDTO>> openTickets(@PathVariable String id) {
        return ResponseEntity.ok(ticketTransferService.openTickets(id));
    }

    @GetMapping("/me/coordination")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Segnalazioni aperte dei progetti coordinati dall'utente, con i Tutor che possono riceverle (US-112)")
    public ResponseEntity<List<TransferGroupDTO>> coordination() {
        return ResponseEntity.ok(ticketTransferService.coordination());
    }

    @PostMapping("/tickets/transfer")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.ADMIN, @requestsAuthorizer.TUTOR)")
    @Operation(summary = "Sposta segnalazioni aperte dello stesso progetto su un altro Tutor, con i beneficiari "
            + "(US-111); un Tutor solo nei progetti che coordina (US-112)")
    public ResponseEntity<TicketTransferResultDTO> transfer(@RequestBody @Valid TicketTransferDTO dto) {
        return ResponseEntity.ok(ticketTransferService.transfer(dto));
    }
}
