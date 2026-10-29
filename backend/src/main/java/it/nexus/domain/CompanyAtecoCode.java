package it.nexus.domain;

import java.io.Serial;

import org.hibernate.Hibernate;

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
 * Codice ATECO di un'azienda (US-506): si aggiunge e si toglie, non si modifica. Nel tirocinio si sceglie quello giusto
 * (US-1001).
 */
@Getter
@Entity
@Table(name = "company_ateco_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyAtecoCode extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, updatable = false)
    private Company company;

    @Column(name = "code", nullable = false, updatable = false, length = 8)
    private String code;

    @Column(name = "description", updatable = false, length = 200)
    private String description;

    public CompanyAtecoCode(Company company, String code, String description) {
        this.company = company;
        this.code = code;
        this.description = description;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((CompanyAtecoCode) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CompanyAtecoCode[id=" + id + "]";
    }
}
