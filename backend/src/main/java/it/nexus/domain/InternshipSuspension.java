package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

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
 * Periodo di sospensione previsto di un tirocinio (US-1001), estremi compresi; senza sovrapposizioni nello stesso
 * tirocinio ({@code ex_internship_suspension_overlap}). Riga sostituita in blocco a ogni salvataggio della scheda.
 */
@Getter
@Entity
@Table(name = "internship_suspension")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternshipSuspension extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_id", nullable = false, updatable = false)
    private Internship internship;

    @Column(name = "date_from", nullable = false, updatable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false, updatable = false)
    private LocalDate dateTo;

    InternshipSuspension(Internship internship, LocalDate dateFrom, LocalDate dateTo) {
        this.internship = internship;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((InternshipSuspension) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "InternshipSuspension[id=" + id + "]";
    }
}
