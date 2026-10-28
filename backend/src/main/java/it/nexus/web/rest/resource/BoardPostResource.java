package it.nexus.web.rest.resource;

import java.net.URI;
import java.util.List;

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
import it.nexus.domain.dto.BoardPostCreateDTO;
import it.nexus.domain.dto.BoardPostDTO;
import it.nexus.domain.dto.BoardPostFormDTO;
import it.nexus.domain.dto.VersionDTO;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.services.BoardPostService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/board-posts")
@RequiredArgsConstructor
@Tag(name = "Bacheca")
public class BoardPostResource {

    private final BoardPostService boardPostService;

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Annunci della bacheca in uno stato, dal più recente (gestione, con l'azienda)")
    public ResponseEntity<List<BoardPostDTO>> list(@RequestParam(defaultValue = "DRAFT") BoardPostStatus status) {
        return ResponseEntity.ok(boardPostService.list(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Dettaglio di un annuncio (gestione, con l'azienda)")
    public ResponseEntity<BoardPostDTO> get(@PathVariable String id) {
        return ResponseEntity.ok(boardPostService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Crea una bozza da una mansione libera (un solo annuncio attivo per mansione)")
    public ResponseEntity<BoardPostDTO> create(@RequestBody @Valid BoardPostCreateDTO dto) {
        BoardPostDTO created = boardPostService.create(dto);
        return ResponseEntity.created(URI.create(PathAPI.BASE_PATH_V1 + "/board-posts/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Modifica un annuncio non archiviato; il progetto solo nella bozza")
    public ResponseEntity<BoardPostDTO> update(@PathVariable String id, @RequestBody @Valid BoardPostFormDTO dto) {
        return ResponseEntity.ok(boardPostService.update(id, dto));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Pubblica una bozza (mansione libera e a disposizione)")
    public ResponseEntity<BoardPostDTO> publish(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(boardPostService.publish(id, body.version()));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Archivia una bozza o un annuncio pubblicato")
    public ResponseEntity<BoardPostDTO> archive(@PathVariable String id, @RequestBody @Valid VersionDTO body) {
        return ResponseEntity.ok(boardPostService.archive(id, body.version()));
    }
}
