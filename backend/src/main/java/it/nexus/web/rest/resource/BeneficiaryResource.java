package it.nexus.web.rest.resource;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.nexus.domain.dto.BeneficiaryCreateDTO;
import it.nexus.domain.dto.BeneficiaryDTO;
import it.nexus.domain.dto.BeneficiarySummaryDTO;
import it.nexus.services.BeneficiaryService;
import it.nexus.web.rest.PathAPI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(PathAPI.BASE_PATH_V1 + "/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiari")
public class BeneficiaryResource {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    @PreAuthorize("hasRole(@requestsAuthorizer.TUTOR)")
    @Operation(summary = "Registra un beneficiario del Tutor corrente")
    public ResponseEntity<BeneficiaryDTO> register(@RequestBody @Valid BeneficiaryCreateDTO dto) {
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
    @Operation(summary = "Dettaglio di un beneficiario (proprietario, Call Center, ADMIN)")
    public ResponseEntity<BeneficiaryDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(beneficiaryService.getById(id));
    }
}
