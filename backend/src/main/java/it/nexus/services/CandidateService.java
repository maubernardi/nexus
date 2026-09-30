package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.CandidateCreateDTO;
import it.nexus.domain.dto.CandidateDTO;
import it.nexus.domain.dto.CandidateSummaryDTO;

public interface CandidateService {

    /** Registra un candidato di proprietà del Tutor corrente. */
    CandidateDTO register(CandidateCreateDTO dto);

    /** Candidati di cui il Tutor corrente è proprietario. */
    List<CandidateSummaryDTO> listMine();

    /** Dettaglio visibile al proprietario, al Call Center e all'ADMIN; altrimenti NotFound. */
    CandidateDTO getById(String id);
}
