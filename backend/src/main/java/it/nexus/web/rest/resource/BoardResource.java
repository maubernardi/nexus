package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.TutorBoardPostDTO;
import it.nexus.services.BoardPostService;
import it.nexus.web.rest.PathAPI;
import lombok.RequiredArgsConstructor;

/** Bacheca come la vede il Tutor (US-901); la gestione del Call Center è in {@link BoardPostResource}. */
@RestController
@RequiredArgsConstructor
@Tag(name = "Bacheca")
public class BoardResource {

    private final BoardPostService boardPostService;

    @GetMapping(PathAPI.BASE_PATH_V1 + "/board")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Annunci pubblicati per tutti o per i progetti del Tutor, senza azienda, titolo e note")
    public ResponseEntity<List<TutorBoardPostDTO>> list(@RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String jobCategoryId) {
        return ResponseEntity.ok(boardPostService.listForTutor(zoneId, jobCategoryId));
    }
}
