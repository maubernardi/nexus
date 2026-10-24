package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.BeneficiaryDTO;
import it.nexus.domain.dto.BeneficiaryFormDTO;
import it.nexus.domain.dto.BeneficiarySummaryDTO;

public interface BeneficiaryService {

    /** Registra un beneficiario di proprietà del Tutor corrente. */
    BeneficiaryDTO register(BeneficiaryFormDTO dto);

    /** Beneficiari di cui il Tutor corrente è proprietario. */
    List<BeneficiarySummaryDTO> listMine();

    /**
     * Dettaglio visibile al proprietario, ai Tutor dei progetti in cui il beneficiario ha segnalazioni, al Call Center e
     * all'ADMIN; altrimenti NotFound (US-204).
     */
    BeneficiaryDTO getById(String id);

    /** Modifica riservata al Tutor proprietario (US-203); nell'audit solo i nomi dei campi cambiati. */
    BeneficiaryDTO update(String id, BeneficiaryFormDTO dto);
}
