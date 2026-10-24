package it.nexus.domain;

import java.io.Serial;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

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
 * Nota del diario di un'azienda (US-505), in sola aggiunta: il database rifiuta UPDATE, DELETE e TRUNCATE e l'entità è
 * {@link Immutable}. Il testo può contenere dati personali: mai nei log né nell'audit. Istante e autore sono
 * {@code createdAt}/{@code createdBy}.
 */
@Getter
@Entity
@Immutable
@Table(name = "company_note")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyNote extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, updatable = false)
    private Company company;

    @Column(name = "text", nullable = false, updatable = false, length = 2000)
    private String text;

    public CompanyNote(Company company, String text) {
        this.company = company;
        this.text = text;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((CompanyNote) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CompanyNote[id=" + id + "]";
    }
}
