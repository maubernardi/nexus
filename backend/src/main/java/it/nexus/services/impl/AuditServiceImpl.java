package it.nexus.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AuditEvent;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.repository.AuditEventRepository;
import it.nexus.services.AuditService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository repository;

    @Override
    public void record(AuditEntityType entityType, Long entityId, String action, AuditChanges changes, String reason) {
        repository.save(new AuditEvent(entityType, entityId, action,
                (changes == null ? AuditChanges.none() : changes).asMap(),
                reason == null || reason.isBlank() ? null : reason.strip()));
    }
}
