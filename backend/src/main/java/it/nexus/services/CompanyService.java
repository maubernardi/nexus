package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.CompanyDTO;
import it.nexus.domain.dto.CompanyFormDTO;

/** Anagrafica delle aziende ospitanti (US-501): Call Center e ADMIN. Nessuna cancellazione, solo disattivazione. */
public interface CompanyService {

    List<CompanyDTO> search(String search, boolean includeInactive);

    CompanyDTO getById(String id);

    CompanyDTO create(CompanyFormDTO dto);

    /** @throws it.nexus.web.errors.ConflictException se la versione è superata */
    CompanyDTO update(String id, CompanyFormDTO dto);

    /** Attiva o disattiva; @throws it.nexus.web.errors.ConflictException se la versione è superata */
    CompanyDTO setActive(String id, long expectedVersion, boolean active);
}
