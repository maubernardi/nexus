package it.nexus.web.rest.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.ClosureFormDTO;
import it.nexus.domain.dto.PatronFormDTO;
import it.nexus.domain.dto.WorkCalendarDTO;
import it.nexus.services.WorkCalendarService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendario")
public class WorkCalendarResource {

    private final WorkCalendarService calendarService;

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Calendario unico: patrono e chiusure")
    public ResponseEntity<WorkCalendarDTO> get() {
        return ResponseEntity.ok(calendarService.get());
    }

    @PutMapping("/patron")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Cambia il patrono (giorno e mese)")
    public ResponseEntity<WorkCalendarDTO> updatePatron(@RequestBody @Valid PatronFormDTO dto) {
        return ResponseEntity.ok(calendarService.updatePatron(dto));
    }

    @PostMapping("/closures")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Aggiunge una chiusura: un giorno o un periodo")
    public ResponseEntity<WorkCalendarDTO> addClosure(@RequestBody @Valid ClosureFormDTO dto) {
        return ResponseEntity.ok(calendarService.addClosure(dto));
    }

    @DeleteMapping("/closures/{id}")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Rimuove una chiusura")
    public ResponseEntity<WorkCalendarDTO> removeClosure(@PathVariable String id) {
        return ResponseEntity.ok(calendarService.removeClosure(id));
    }
}
