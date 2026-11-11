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
 * Progetto formativo della convenzione (US-1008): date proprie dentro quelle del tirocinio, mansione e dettaglio; il
 * resto (orario compreso) è della convenzione e del tirocinio. Il numero è la posizione nell'ordine delle date, senza
 * sovrapposizioni ({@code ex_internship_training_project_overlap}). Riga sostituita in blocco a ogni salvataggio.
 */
@Getter
@Entity
@Table(name = "internship_training_project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternshipTrainingProject extends AbstractCreationAuditingEntity {

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

    @Column(name = "job_title", nullable = false, updatable = false, length = 200)
    private String jobTitle;

    /** Testo libero: nell'audit solo come «cambiato». */
    @Column(name = "job_details", updatable = false, length = 2000)
    private String jobDetails;

    InternshipTrainingProject(Internship internship, LocalDate dateFrom, LocalDate dateTo, String jobTitle,
            String jobDetails) {
        this.internship = internship;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.jobTitle = jobTitle;
        this.jobDetails = jobDetails;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((InternshipTrainingProject) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "InternshipTrainingProject[id=" + id + "]";
    }
}
