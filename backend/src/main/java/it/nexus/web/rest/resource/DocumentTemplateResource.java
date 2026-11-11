package it.nexus.web.rest.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.DocumentTemplateActiveDTO;
import it.nexus.domain.dto.DocumentTemplateDTO;
import it.nexus.domain.dto.DocumentTemplateFormDTO;
import it.nexus.services.DocumentTemplateService;
import it.nexus.services.DownloadedFile;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Modelli Word dei documenti caricati dall'ADMIN (US-1010). */
@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/document-templates")
@RequiredArgsConstructor
@Tag(name = "Modelli dei documenti")
public class DocumentTemplateResource {

    private static final MediaType DOCX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final DocumentTemplateService service;

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Modelli caricati, attivi e disattivati")
    public ResponseEntity<List<DocumentTemplateDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/placeholders")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Segnaposto che i modelli possono usare")
    public ResponseEntity<List<String>> placeholders() {
        return ResponseEntity.ok(service.placeholders());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Carica un modello .docx con documento, progetto, tipo di tirocinio, ente (facoltativo) e nome")
    public ResponseEntity<DocumentTemplateDTO> upload(@RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "document", required = false) String document,
            @RequestParam(value = "projectId", required = false) String projectId,
            @RequestParam(value = "internshipTypeId", required = false) String internshipTypeId,
            @RequestParam(value = "careAgencyId", required = false) String careAgencyId,
            @RequestParam(value = "name", required = false) String name) throws IOException {
        DocumentTemplateDTO created = service.upload(
                new DocumentTemplateFormDTO(document, projectId, internshipTypeId, careAgencyId, name),
                file == null ? null : file.getOriginalFilename(), file == null ? null : file.getBytes());
        return ResponseEntity.created(URI.create(PathAPI.BASE_PATH_V1 + "/document-templates/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Scarica il file del modello")
    public ResponseEntity<byte[]> file(@PathVariable String id) {
        DownloadedFile file = service.file(id);
        return ResponseEntity.ok()
                .contentType(DOCX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.name(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(file.content());
    }

    @PutMapping(path = "/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Sostituisce il file del modello, con gli stessi controlli del caricamento")
    public ResponseEntity<DocumentTemplateDTO> replaceFile(@PathVariable String id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "version", required = false) Long version) throws IOException {
        return ResponseEntity.ok(service.replaceFile(id, version, file == null ? null : file.getOriginalFilename(),
                file == null ? null : file.getBytes()));
    }

    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Disattiva o riattiva il modello")
    public ResponseEntity<DocumentTemplateDTO> setActive(@PathVariable String id,
            @RequestBody @Valid DocumentTemplateActiveDTO dto) {
        return ResponseEntity.ok(service.setActive(id, dto));
    }
}
