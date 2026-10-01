package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.JobSlotDTO;
import it.nexus.domain.dto.JobSlotFormDTO;

/** Mansioni delle aziende (US-502). Lo stato LIBERA/BLOCCATA lo governano abbinamenti e chiusure, non le modifiche. */
public interface JobSlotService {

    List<JobSlotDTO> listByCompany(String companyId);

    /** @throws it.nexus.web.errors.ConflictException se l'azienda è disattivata */
    JobSlotDTO create(String companyId, JobSlotFormDTO dto);

    /** @throws it.nexus.web.errors.ConflictException se la versione è superata */
    JobSlotDTO update(String id, JobSlotFormDTO dto);

    /** Ritira o rimette a disposizione; una mansione bloccata non si ritira (409). */
    JobSlotDTO setActive(String id, long expectedVersion, boolean active);
}
