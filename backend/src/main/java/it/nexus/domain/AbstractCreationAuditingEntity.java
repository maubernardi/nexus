package it.nexus.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Audit di sola creazione ({@code created_at/by}), per le righe che non si modificano: associazioni, blacklist,
 * audit trail. Valorizzato da Spring Data JPA Auditing con l'utente autenticato o {@code system}.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractCreationAuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;
}
