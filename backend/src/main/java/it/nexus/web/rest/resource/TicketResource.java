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
import it.nexus.domain.dto.AppointmentFailedDTO;
import it.nexus.domain.dto.AppointmentSetDTO;
import it.nexus.domain.dto.ApproveCategoryDTO;
import it.nexus.domain.dto.CompanyExclusionDTO;
import it.nexus.domain.dto.CompanyExclusionFormDTO;
import it.nexus.domain.dto.JobSlotMatchDTO;
import it.nexus.domain.dto.MatchRequestDTO;
import it.nexus.domain.dto.ProposalRejectDTO;
import it.nexus.domain.dto.RejectCategoryDTO;
import it.nexus.domain.dto.TicketListItemDTO;
import it.nexus.domain.dto.TicketCreateDTO;
import it.nexus.domain.dto.TicketSpecialCreateDTO;
import it.nexus.domain.dto.TicketDTO;
import it.nexus.domain.dto.TicketDetailDTO;
import it.nexus.domain.dto.TicketEventDTO;
import it.nexus.domain.dto.VersionDTO;
import it.nexus.domain.enumeration.TicketListView;
import it.nexus.domain.enumeration.TicketStatus;
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

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Elenco delle segnalazioni della Home: vista per ruolo, filtri per stato e progetto")
    public ResponseEntity<List<TicketListItemDTO>> list(@RequestParam(required = false) TicketListView view,
            @RequestParam(required = false) TicketStatus status, @RequestParam(required = false) String projectId) {
        return ResponseEntity.ok(ticketService.list(view, status, projectId));
    }

    @PostMapping("/{id}/take-charge")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Prende in carico una segnalazione dalla coda (409 se già presa o modificata)")
    public ResponseEntity<TicketListItemDTO> takeCharge(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(ticketService.takeCharge(id, body.version()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Dettaglio della segnalazione (Tutor: le proprie e dei suoi progetti)")
    public ResponseEntity<TicketDetailDTO> getDetail(@PathVariable String id) {
        return ResponseEntity.ok(ticketService.getDetail(id));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Cronologia della segnalazione: data, autore, azione, stato e nota")
    public ResponseEntity<List<TicketEventDTO>> history(@PathVariable String id) {
        return ResponseEntity.ok(ticketService.history(id));
    }

    @GetMapping("/{id}/exclusions")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Aziende escluse per la segnalazione, con motivo")
    public ResponseEntity<List<CompanyExclusionDTO>> exclusions(@PathVariable String id) {
        return ResponseEntity.ok(ticketService.exclusions(id));
    }

    @PostMapping("/{id}/exclusions")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Esclude un'azienda per la segnalazione, con motivo: non compare più tra le compatibili")
    public ResponseEntity<List<CompanyExclusionDTO>> excludeCompany(@PathVariable String id,
            @RequestBody @Valid CompanyExclusionFormDTO body) {
        return ResponseEntity.ok(ticketService.excludeCompany(id, body));
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

    @PostMapping("/{id}/take-over")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Sposta su di sé una segnalazione in carico a un collega")
    public ResponseEntity<TicketDetailDTO> takeOver(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(ticketService.takeOver(id, body.version()));
    }

    @PostMapping("/{id}/category/approve")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Approva la nuova tipologia proposta dal Tutor: entra nel catalogo e la segnalazione va in coda")
    public ResponseEntity<TicketDetailDTO> approveCategory(@PathVariable String id,
            @RequestBody @Valid ApproveCategoryDTO body) {
        return ResponseEntity.ok(ticketService.approveCategory(id, body));
    }

    @PostMapping("/{id}/category/reject")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Respinge la nuova tipologia: si usa quella del catalogo indicata, con il motivo per il Tutor")
    public ResponseEntity<TicketDetailDTO> rejectCategory(@PathVariable String id,
            @RequestBody @Valid RejectCategoryDTO body) {
        return ResponseEntity.ok(ticketService.rejectCategory(id, body));
    }

    @PostMapping("/{id}/appointment-set")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Il Tutor della segnalazione registra l'appuntamento fissato con l'azienda: il timer si ferma")
    public ResponseEntity<TicketDetailDTO> appointmentSet(@PathVariable String id,
            @RequestBody @Valid AppointmentSetDTO body) {
        return ResponseEntity.ok(ticketService.appointmentSet(id, body));
    }

    @PostMapping("/{id}/appointment-failed")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Il Tutor della segnalazione registra l'appuntamento fallito: la segnalazione torna al Call Center")
    public ResponseEntity<TicketDetailDTO> appointmentFailed(@PathVariable String id,
            @RequestBody @Valid AppointmentFailedDTO body) {
        return ResponseEntity.ok(ticketService.appointmentFailed(id, body));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Il Tutor della segnalazione rifiuta la proposta con motivo: l'azienda è esclusa")
    public ResponseEntity<TicketDetailDTO> rejectProposal(@PathVariable String id,
            @RequestBody @Valid ProposalRejectDTO body) {
        return ResponseEntity.ok(ticketService.rejectProposal(id, body));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Il Tutor della segnalazione accetta la proposta: parte il timer di 7 giorni lavorativi")
    public ResponseEntity<TicketDetailDTO> accept(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(ticketService.accept(id, body.version()));
    }

    @PostMapping("/special")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Candida un beneficiario del Tutor a un annuncio della bacheca: segnalazione speciale (US-306)")
    public ResponseEntity<TicketDTO> submitSpecial(@RequestBody @Valid TicketSpecialCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.submitSpecial(dto));
    }

    // nessun Location: il dettaglio del ticket arriva con US-305
    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Invia una segnalazione normale per un beneficiario del Tutor corrente")
    public ResponseEntity<TicketDTO> submit(@RequestBody @Valid TicketCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.submit(dto));
    }
}
