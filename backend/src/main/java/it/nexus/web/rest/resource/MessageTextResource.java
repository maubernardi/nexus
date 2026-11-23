package it.nexus.web.rest.resource;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.MessageTextDTO;
import it.nexus.domain.dto.MessageTextUpdateDTO;
import it.nexus.services.MessageTextService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Testi delle notifiche e delle email, modificabili dall'ADMIN (US-1204). */
@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/message-texts")
@RequiredArgsConstructor
@Tag(name = "Testi dei messaggi")
public class MessageTextResource {

    private final MessageTextService messageTextService;

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Messaggi con i testi in uso e i predefiniti")
    public ResponseEntity<List<MessageTextDTO>> list() {
        return ResponseEntity.ok(messageTextService.list());
    }

    @PutMapping("/{key}/{part}")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Cambia il testo di una parte di un messaggio")
    public ResponseEntity<MessageTextDTO> update(@PathVariable String key, @PathVariable String part,
            @RequestBody @Valid MessageTextUpdateDTO body) {
        return ResponseEntity.ok(messageTextService.update(key, part, body.text()));
    }

    @DeleteMapping("/{key}/{part}")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Ripristina il testo predefinito di una parte di un messaggio")
    public ResponseEntity<MessageTextDTO> reset(@PathVariable String key, @PathVariable String part) {
        return ResponseEntity.ok(messageTextService.reset(key, part));
    }
}
