package it.nexus.domain;

import java.io.Serial;

import org.hibernate.Hibernate;

import io.hypersistence.tsid.TSID;

import it.nexus.domain.id.TsidId;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Base delle entità con chiave primaria TSID. Uguaglianza basata sull'id (per entità già persistite) e hashCode
 * costante per classe, così l'entità resta coerente nei Set prima e dopo il persist. {@code toString()} riporta solo
 * tipo e id: le sottoclassi non devono aggiungere dati personali (niente nei log).
 */
@Getter
@MappedSuperclass
public abstract class AbstractTsidEntity extends AbstractAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((AbstractTsidEntity) other).id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }

    @Override
    public String toString() {
        return Hibernate.getClass(this).getSimpleName() + "[id=" + externalId() + "]";
    }

    /** Id nella forma esposta dalle API (TSID a 13 caratteri), {@code null} prima del persist. */
    protected String externalId() {
        return id == null ? null : TSID.from(id).toString();
    }
}
