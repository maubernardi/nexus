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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import it.nexus.domain.dto.BeneficiaryDTO;
import it.nexus.domain.dto.BeneficiaryFormDTO;
import it.nexus.domain.dto.BeneficiaryKeepDTO;
import it.nexus.domain.dto.BeneficiaryRetentionItemDTO;
import it.nexus.domain.dto.BeneficiarySummaryDTO;
import it.nexus.services.BeneficiaryRetentionService;
import it.nexus.services.BeneficiaryService;
import it.nexus.services.DownloadedFile;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiari")
public class BeneficiaryResource {

    private final BeneficiaryService beneficiaryService;
    private final BeneficiaryRetentionService beneficiaryRetentionService;

    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Registra un beneficiario del Tutor corrente")
    public ResponseEntity<BeneficiaryDTO> register(@RequestBody @Valid BeneficiaryFormDTO dto) {
        BeneficiaryDTO created = beneficiaryService.register(dto);
        return ResponseEntity.created(URI.create(PathAPI.BASE_PATH_V1 + "/beneficiaries/" + created.id())).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Beneficiari di cui il Tutor corrente è proprietario")
    public ResponseEntity<List<BeneficiarySummaryDTO>> listMine() {
        return ResponseEntity.ok(beneficiaryService.listMine());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Dettaglio di un beneficiario (proprietario, Tutor dei progetti delle sue segnalazioni, Call Center, ADMIN)")
    public ResponseEntity<BeneficiaryDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(beneficiaryService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Modifica un beneficiario del Tutor corrente")
    public ResponseEntity<BeneficiaryDTO> update(@PathVariable String id, @RequestBody @Valid BeneficiaryFormDTO dto) {
        return ResponseEntity.ok(beneficiaryService.update(id, dto));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole(@requestsAuthorizer.ADMIN)")
    @Operation(summary = "Tutti i beneficiari con la conservazione dei dati; solo i da anonimizzare con toAnonymize (US-1303)")
    public ResponseEntity<List<BeneficiaryRetentionItemDTO>> listAll(
            @RequestParam(defaultValue = "false") boolean toAnonymize) {
        return ResponseEntity.ok(beneficiaryRetentionService.listAll(toAnonymize));
    }

    @PostMapping("/{id}/anonymize")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Anonimizza un beneficiario da anonimizzare: restano le iniziali e i dati statistici (US-1303)")
    public ResponseEntity<Void> anonymize(@PathVariable String id) {
        beneficiaryRetentionService.anonymize(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/keep")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Tiene per sempre i dati di un beneficiario da anonimizzare, con il motivo (US-1303)")
    public ResponseEntity<Void> keep(@PathVariable String id, @RequestBody @Valid BeneficiaryKeepDTO dto) {
        beneficiaryRetentionService.keep(id, dto.reason());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{id}/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Carica o sostituisce il CV del beneficiario: PDF fino a 5 MB, solo il Tutor proprietario (US-205)")
    public ResponseEntity<BeneficiaryDTO> uploadCv(@PathVariable String id, @RequestParam("file") MultipartFile file)
            throws IOException {
        return ResponseEntity.ok(beneficiaryService.uploadCv(id, file.getOriginalFilename(), file.getBytes()));
    }

    @GetMapping("/{id}/cv")
    @PreAuthorize("hasAnyRole(@requestsAuthorizer.TUTOR, @requestsAuthorizer.CALL_CENTER, @requestsAuthorizer.ADMIN)")
    @Operation(summary = "Scarica il CV, per chi vede il beneficiario (US-205)")
    public ResponseEntity<byte[]> downloadCv(@PathVariable String id) {
        DownloadedFile cv = beneficiaryService.downloadCv(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                // sempre come allegato: il browser non lo apre dentro la pagina dell'app
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(cv.name(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(cv.content());
    }

    @DeleteMapping("/{id}/cv")
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Toglie il CV del beneficiario, solo il Tutor proprietario (US-205)")
    public ResponseEntity<BeneficiaryDTO> deleteCv(@PathVariable String id) {
        return ResponseEntity.ok(beneficiaryService.deleteCv(id));
    }
}
