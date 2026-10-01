package it.nexus.domain;

import java.io.Serial;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.id.TsidId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evento di audit (EN-2): chi ({@code createdBy}), quando ({@code createdAt}), su cosa, quale azione, con quali
 * modifiche e perché. Immutabile, anche nel database (trigger).
 */
@Getter
@Entity
@Immutable
@Table(name = "audit_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEvent extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, updatable = false, length = 40)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false, updatable = false)
    private Long entityId;

    /** Azione in MAIUSCOLO_CON_UNDERSCORE: nome della transizione del ticket oppure CREATE, UPDATE, … */
    @Column(name = "action", nullable = false, updatable = false, length = 60)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes", nullable = false, updatable = false)
    private Map<String, Object> changes = Map.of();

    @Column(name = "reason", updatable = false)
    private String reason;

    public AuditEvent(AuditEntityType entityType, Long entityId, String action, Map<String, Object> changes, String reason) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.changes = changes == null ? Map.of() : changes;
        this.reason = reason;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((AuditEvent) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AuditEvent[id=" + id + ", entityType=" + entityType + ", action=" + action + "]";
    }
}
