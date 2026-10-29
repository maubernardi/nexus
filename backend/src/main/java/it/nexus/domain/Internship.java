package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Scheda amministrativa del tirocinio (US-1001), una per segnalazione: la compila il Tutor dopo l'appuntamento fissato.
 * Campi segnaposto fino ai moduli ufficiali (Q5), tutti facoltativi. Contiene dati personali del beneficiario e dei
 * tutor aziendali: {@code toString()} senza valori (da {@link AbstractTsidEntity}), mai nei log.
 */
@Getter
@Setter
@Entity
@Table(name = "internship")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Internship extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true, updatable = false)
    @Setter(AccessLevel.NONE)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_type_id")
    private InternshipType internshipType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "weekly_hours")
    private Integer weeklyHours;

    @Column(name = "site", length = 300)
    private String site;

    /** Codice scelto tra quelli dell'azienda della segnalazione (US-506). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ateco_code_id")
    private CompanyAtecoCode atecoCode;

    @Column(name = "fiscal_code", length = 16)
    private String fiscalCode;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_place", length = 200)
    private String birthPlace;

    @Column(name = "residence", length = 300)
    private String residence;

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<InternshipCompanyTutor> companyTutors = new ArrayList<>();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateFrom")
    private List<InternshipSuspension> suspensions = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    public Internship(Ticket ticket) {
        this.ticket = ticket;
    }

    public void addCompanyTutor(String fullName, String contact) {
        companyTutors.add(new InternshipCompanyTutor(this, fullName, contact));
    }

    public void addSuspension(LocalDate from, LocalDate to) {
        suspensions.add(new InternshipSuspension(this, from, to));
    }

    /**
     * Appuntamento non andato a buon fine (D7): i dati del tirocinio riguardavano quell'azienda e si azzerano; restano
     * quelli del beneficiario.
     */
    public void clearCompanyData() {
        internshipType = null;
        startDate = null;
        expectedEndDate = null;
        weeklyHours = null;
        site = null;
        atecoCode = null;
        notes = null;
        companyTutors.clear();
        suspensions.clear();
    }
}
