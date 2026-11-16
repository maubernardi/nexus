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
 * Proroga del tirocinio (US-1012): una convenzione in tutto e per tutto, con numero, data, data di stipula e date
 * proprie, successive alla convenzione precedente; gli altri dati sono quelli della scheda. Numerata nell'ordine delle
 * date, senza sovrapposizioni ({@code ex_internship_extension_overlap}). Riga sostituita in blocco a ogni salvataggio.
 */
@Getter
@Entity
@Table(name = "internship_extension")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternshipExtension extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_id", nullable = false, updatable = false)
    private Internship internship;

    @Column(name = "agreement_number", nullable = false, updatable = false, length = 50)
    private String agreementNumber;

    @Column(name = "agreement_date", nullable = false, updatable = false)
    private LocalDate agreementDate;

    @Column(name = "agreement_signed_date", nullable = false, updatable = false)
    private LocalDate agreementSignedDate;

    @Column(name = "date_from", nullable = false, updatable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false, updatable = false)
    private LocalDate dateTo;

    InternshipExtension(Internship internship, String agreementNumber, LocalDate agreementDate,
            LocalDate agreementSignedDate, LocalDate dateFrom, LocalDate dateTo) {
        this.internship = internship;
        this.agreementNumber = agreementNumber;
        this.agreementDate = agreementDate;
        this.agreementSignedDate = agreementSignedDate;
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
        return id != null && id.equals(((InternshipExtension) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "InternshipExtension[id=" + id + "]";
    }
}
