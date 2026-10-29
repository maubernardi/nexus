package it.nexus.services;

import java.util.List;
import java.util.Optional;

import it.nexus.domain.dto.AtecoCodeDTO;
import it.nexus.domain.dto.AtecoCodeFormDTO;
import it.nexus.domain.dto.LegalRepresentativeDTO;
import it.nexus.domain.dto.LegalRepresentativeFormDTO;

/**
 * Dati dell'azienda per i documenti dei tirocini (US-506): legale rappresentante e codici ATECO. Il Call Center e
 * l'ADMIN li gestiscono dalla scheda dell'azienda; il Tutor li userà dalla scheda del tirocinio (US-1001).
 */
public interface CompanyLegalDataService {

    Optional<LegalRepresentativeDTO> legalRepresentative(String companyId);

    /**
     * Registra o corregge il legale rappresentante.
     *
     * @throws it.nexus.web.errors.ConflictException se la versione è superata
     */
    LegalRepresentativeDTO saveLegalRepresentative(String companyId, LegalRepresentativeFormDTO dto);

    List<AtecoCodeDTO> atecoCodes(String companyId);

    /** @throws it.nexus.web.errors.FieldValidationException se l'azienda ha già quel codice */
    AtecoCodeDTO addAtecoCode(String companyId, AtecoCodeFormDTO dto);

    void removeAtecoCode(String companyId, String atecoCodeId);
}
