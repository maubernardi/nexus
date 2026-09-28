package it.nexus.domain;

import java.io.Serial;
import java.time.Instant;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Colonne di audit complete: creazione ({@link AbstractCreationAuditingEntity}) e ultima modifica
 * ({@code updated_at/by}). Le date sono istanti UTC ({@code timestamptz}).
 */
@Getter
@MappedSuperclass
public abstract class AbstractAuditingEntity extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;
}
