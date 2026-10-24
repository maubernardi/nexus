package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.CompanyNoteDTO;
import it.nexus.domain.dto.CompanyNoteFormDTO;

public interface CompanyNoteService {

    /** Note dell'azienda dalla più recente. */
    List<CompanyNoteDTO> list(String companyId);

    /** Aggiunge una nota; nell'audit solo l'azione, senza testo. */
    CompanyNoteDTO add(String companyId, CompanyNoteFormDTO dto);
}
