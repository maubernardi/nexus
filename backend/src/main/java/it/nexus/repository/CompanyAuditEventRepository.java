package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import it.nexus.domain.CompanyAuditEvent;

/**
 * Audit trail in sola aggiunta: espone solo inserimento e lettura, nessun metodo di modifica o cancellazione
 * (che il database comunque rifiuta).
 */
@org.springframework.stereotype.Repository
public interface CompanyAuditEventRepository extends Repository<CompanyAuditEvent, Long> {

    CompanyAuditEvent save(CompanyAuditEvent event);

    Optional<CompanyAuditEvent> findById(Long id);

    List<CompanyAuditEvent> findByCompanyIdOrderByCreatedAtAsc(Long companyId);
}
