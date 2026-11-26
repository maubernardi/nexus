package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.CompanyDTO;
import it.nexus.domain.dto.CompanyDirectoryPageDTO;
import it.nexus.domain.dto.CompanyFormDTO;
import it.nexus.domain.dto.CompanyPageDTO;

/** Anagrafica delle aziende ospitanti (US-501): Call Center e ADMIN. Nessuna cancellazione, solo disattivazione. */
public interface CompanyService {

    /** Aziende per pagina ammesse nell'elenco (US-513, PO 24/11). */
    List<Integer> PAGE_SIZES = List.of(10, 25, 50, 100);

    /** Aziende per pagina se non è indicato (US-513). */
    int DEFAULT_PAGE_SIZE = 25;

    /**
     * Una pagina dell'elenco con i filtri, ordinato per nome; {@code page} parte da 0 (US-513).
     *
     * @throws it.nexus.web.errors.BadRequestException se la pagina è negativa, la dimensione non è tra
     *         {@link #PAGE_SIZES}, o stelle e pollici minimi sono fuori da 1–4 e 1–2 (US-514)
     */
    CompanyPageDTO search(CompanySearch filters, int page, int size);

    /**
     * Elenco del Tutor (US-511): sole aziende attive, con i dati essenziali; ricerca sul solo nome, filtri per categoria
     * merceologica e zona. Pagine come {@link #search}.
     *
     * @throws it.nexus.web.errors.BadRequestException se la pagina è negativa o la dimensione non è tra
     *         {@link #PAGE_SIZES}
     */
    CompanyDirectoryPageDTO directory(String search, String businessCategoryId, String zoneId, int page, int size);

    CompanyDTO getById(String id);

    CompanyDTO create(CompanyFormDTO dto);

    /** @throws it.nexus.web.errors.ConflictException se la versione è superata */
    CompanyDTO update(String id, CompanyFormDTO dto);

    /** Attiva o disattiva; @throws it.nexus.web.errors.ConflictException se la versione è superata */
    CompanyDTO setActive(String id, long expectedVersion, boolean active);
}
