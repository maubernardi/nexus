package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.BeneficiaryCreateDTO;
import it.nexus.domain.dto.BeneficiaryDTO;
import it.nexus.domain.dto.BeneficiarySummaryDTO;

public interface BeneficiaryService {

    /** Registra un beneficiario di proprietà del Tutor corrente. */
    BeneficiaryDTO register(BeneficiaryCreateDTO dto);

    /** Beneficiari di cui il Tutor corrente è proprietario. */
    List<BeneficiarySummaryDTO> listMine();

    /** Dettaglio visibile al proprietario, al Call Center e all'ADMIN; altrimenti NotFound. */
    BeneficiaryDTO getById(String id);
}
