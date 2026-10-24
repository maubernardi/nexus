package it.nexus.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import it.nexus.domain.CompanyNote;

/**
 * Diario delle note in sola aggiunta: espone solo inserimento e lettura, nessun metodo di modifica o cancellazione
 * (che il database comunque rifiuta).
 */
@org.springframework.stereotype.Repository
public interface CompanyNoteRepository extends Repository<CompanyNote, Long> {

    CompanyNote save(CompanyNote note);

    /** Note dell'azienda dalla più recente. */
    List<CompanyNote> findByCompanyIdOrderByCreatedAtDescIdDesc(Long companyId);
}
