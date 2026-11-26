package it.nexus.web.rest.resource;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.CompanyDTO;
import it.nexus.domain.dto.CompanyDirectoryItemDTO;
import it.nexus.domain.dto.CompanyDirectoryPageDTO;
import it.nexus.domain.dto.CompanyReviewsDTO;
import it.nexus.domain.dto.CompanyHistoryDTO;
import it.nexus.domain.dto.CompanyFormDTO;
import it.nexus.domain.dto.CompanyPageDTO;
import it.nexus.domain.dto.VersionDTO;
import it.nexus.services.CompanyHistoryService;
import it.nexus.services.CompanyReviewService;
import it.nexus.services.CompanySearch;
import it.nexus.services.CompanyService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/companies")
@RequiredArgsConstructor
@Tag(name = "Aziende")
public class CompanyResource {

    private final CompanyService companyService;
    private final CompanyHistoryService companyHistoryService;
    private final CompanyReviewService companyReviewService;

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Cerca aziende per ragione sociale o partita IVA, una pagina alla volta (page da 0; size 10, "
            + "25, 50 o 100); filtri: da completare, categoria merceologica, zona, stelle (1–4) e pollici (1–2) minimi, "
            + "solo le navi scuola (US-515)")
    public ResponseEntity<CompanyPageDTO> search(@RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "false") boolean toComplete,
            @RequestParam(required = false) String businessCategoryId,
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) Integer minReliability,
            @RequestParam(required = false) Integer minProjectInterest,
            @RequestParam(defaultValue = "false") boolean trainingShip,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + CompanyService.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(companyService.search(new CompanySearch(search, includeInactive, toComplete,
                businessCategoryId, zoneId, minReliability, minProjectInterest, trainingShip), page, size));
    }

    @GetMapping("/directory")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Aziende attive per il Tutor, con i soli dati essenziali, una pagina alla volta (US-511): "
            + "ricerca sul nome, filtri per categoria merceologica e zona")
    public ResponseEntity<CompanyDirectoryPageDTO> directory(@RequestParam(required = false) String search,
            @RequestParam(required = false) String businessCategoryId,
            @RequestParam(required = false) String zoneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + CompanyService.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(companyService.directory(search, businessCategoryId, zoneId, page, size));
    }

    @GetMapping("/directory/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Un'azienda attiva per la scheda in sola lettura del Tutor, con i dati dell'elenco (US-1104)")
    public ResponseEntity<CompanyDirectoryItemDTO> directoryItem(@PathVariable String id) {
        return ResponseEntity.ok(companyService.directoryItem(id));
    }

    @GetMapping("/{id}/reviews")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Valutazioni dei tirocini dell'azienda, dalla più recente, 10 per pagina (page da 0), con la "
            + "media delle medie (US-1104)")
    public ResponseEntity<CompanyReviewsDTO> reviews(@PathVariable String id,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(companyReviewService.reviews(id, page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Dettaglio di un'azienda")
    public ResponseEntity<CompanyDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Registra un'azienda")
    public ResponseEntity<CompanyDTO> create(@RequestBody @Valid CompanyFormDTO dto) {
        CompanyDTO created = companyService.create(dto);
        return ResponseEntity.created(URI.create(PathAPI.BASE_PATH_V1 + "/companies/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Modifica un'azienda (409 se modificata nel frattempo)")
    public ResponseEntity<CompanyDTO> update(@PathVariable String id, @RequestBody @Valid CompanyFormDTO dto) {
        return ResponseEntity.ok(companyService.update(id, dto));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Storico e pregressi dell'azienda: riepilogo ed eventi dalla più recente, esclusioni comprese")
    public ResponseEntity<CompanyHistoryDTO> history(@PathVariable String id) {
        return ResponseEntity.ok(companyHistoryService.history(id));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Disattiva un'azienda: resta nello storico ma non si propone più")
    public ResponseEntity<CompanyDTO> deactivate(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(companyService.setActive(id, body.version(), false));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Riattiva un'azienda")
    public ResponseEntity<CompanyDTO> activate(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(companyService.setActive(id, body.version(), true));
    }
}
