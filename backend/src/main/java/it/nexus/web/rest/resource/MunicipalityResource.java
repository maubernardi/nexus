package it.nexus.web.rest.resource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.MunicipalityDTO;
import it.nexus.domain.dto.MunicipalityImportPreviewDTO;
import it.nexus.domain.dto.MunicipalityStatusDTO;
import it.nexus.services.MunicipalityService;
import it.nexus.web.rest.PathAPI;
import lombok.RequiredArgsConstructor;

/** Comuni e province dall'elenco dell'ISTAT (US-114): ricerca per tutti, caricamento del file per l'ADMIN. */
@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/municipalities")
@RequiredArgsConstructor
@Tag(name = "Comuni e province")
public class MunicipalityResource {

    private final MunicipalityService municipalityService;

    @GetMapping
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Comuni validi oggi il cui nome contiene il testo (dalla seconda lettera)")
    public ResponseEntity<List<MunicipalityDTO>> search(@RequestParam(name = "q", defaultValue = "") String q) {
        return ResponseEntity.ok(municipalityService.search(q, false));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Comuni il cui nome contiene il testo, con le versioni passate")
    public ResponseEntity<List<MunicipalityDTO>> history(@RequestParam(name = "q", defaultValue = "") String q) {
        return ResponseEntity.ok(municipalityService.search(q, true));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Comuni e province validi oggi e ultimo caricamento")
    public ResponseEntity<MunicipalityStatusDTO> status() {
        return ResponseEntity.ok(municipalityService.status());
    }

    @PostMapping(path = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Differenze tra il file ISTAT e l'elenco attuale, senza salvare")
    public ResponseEntity<MunicipalityImportPreviewDTO> preview(@RequestParam("file") MultipartFile file)
            throws IOException {
        return ResponseEntity.ok(municipalityService.preview(file.getBytes()));
    }

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Carica il file ISTAT dalla data indicata (chiude le versioni cambiate e apre le nuove)")
    public ResponseEntity<MunicipalityImportPreviewDTO> importFile(@RequestParam("file") MultipartFile file,
            @RequestParam("validFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom)
            throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(municipalityService.importFile(file.getBytes(), validFrom));
    }
}
