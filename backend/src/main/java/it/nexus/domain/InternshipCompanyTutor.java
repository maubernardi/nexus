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
 * Tutor aziendale di un tirocinio (US-1001): riga sostituita in blocco a ogni salvataggio della scheda, mai modificata.
 * Dati personali: {@code toString()} senza valori.
 */
@Getter
@Entity
@Table(name = "internship_company_tutor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternshipCompanyTutor extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_id", nullable = false, updatable = false)
    private Internship internship;

    @Column(name = "full_name", nullable = false, updatable = false, length = 200)
    private String fullName;

    @Column(name = "contact", updatable = false, length = 200)
    private String contact;

    InternshipCompanyTutor(Internship internship, String fullName, String contact) {
        this.internship = internship;
        this.fullName = fullName;
        this.contact = contact;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((InternshipCompanyTutor) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "InternshipCompanyTutor[id=" + id + "]";
    }
}
