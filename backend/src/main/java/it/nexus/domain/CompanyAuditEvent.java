package it.nexus.domain;

import java.io.Serial;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.nexus.domain.id.TsidId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evento dell'audit trail di un'azienda, in sola aggiunta: il database rifiuta UPDATE, DELETE e TRUNCATE e
 * l'entità è {@link Immutable}. Istante e autore sono {@code createdAt}/{@code createdBy}.
 */
@Getter
@Entity
@Immutable
@Table(name = "company_audit_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyAuditEvent extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", updatable = false)
    private Ticket ticket;

    /** Tipo di evento in MAIUSCOLO_CON_UNDERSCORE (es. TIROCINIO_CONCLUSO). */
    @Column(name = "event_type", nullable = false, updatable = false, length = 50)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", nullable = false, updatable = false)
    private Map<String, Object> details = Map.of();

    public CompanyAuditEvent(Company company, Ticket ticket, String eventType, Map<String, Object> details) {
        this.company = company;
        this.ticket = ticket;
        this.eventType = eventType;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((CompanyAuditEvent) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CompanyAuditEvent[id=" + id + ", eventType=" + eventType + "]";
    }
}
