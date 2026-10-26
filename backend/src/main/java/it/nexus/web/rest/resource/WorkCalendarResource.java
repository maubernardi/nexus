package it.nexus.web.rest.resource;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import it.nexus.domain.dto.ClosureFormDTO;
import it.nexus.domain.dto.DeadlinePreviewDTO;
import it.nexus.domain.dto.PatronFormDTO;
import it.nexus.domain.dto.WorkCalendarDTO;
import it.nexus.services.WorkCalendarService;
import it.nexus.services.WorkingDayService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendario")
public class WorkCalendarResource {

    /** Giorni lavorativi del timer della proposta accettata (brief, fase 5): promemoria e scadenza. */
    static final int REMINDER_DAYS = 4;
    static final int DEADLINE_DAYS = 7;

    private final WorkCalendarService calendarService;
    private final WorkingDayService workingDayService;

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Calendario unico: patrono e chiusure")
    public ResponseEntity<WorkCalendarDTO> get() {
        return ResponseEntity.ok(calendarService.get());
    }

    @GetMapping("/deadline-preview")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Prova del calcolo: promemoria (4° giorno lavorativo) e scadenza (7°) dopo la data indicata")
    public ResponseEntity<DeadlinePreviewDTO> deadlinePreview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return ResponseEntity.ok(new DeadlinePreviewDTO(from, workingDayService.plusWorkingDays(from, REMINDER_DAYS),
                workingDayService.plusWorkingDays(from, DEADLINE_DAYS)));
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
