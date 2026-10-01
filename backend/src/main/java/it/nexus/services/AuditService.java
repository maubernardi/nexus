package it.nexus.services;

import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.enumeration.AuditEntityType;

/**
 * Registro di audit (EN-2). Va chiamato dentro la transazione dell'operazione registrata: l'evento si salva con
 * l'operazione o per niente.
 */
public interface AuditService {

    /**
     * @param reason motivo o nota facoltativi (vuoto = nessuno)
     * @throws org.springframework.transaction.IllegalTransactionStateException se manca una transazione attiva
     */
    void record(AuditEntityType entityType, Long entityId, String action, AuditChanges changes, String reason);
}
