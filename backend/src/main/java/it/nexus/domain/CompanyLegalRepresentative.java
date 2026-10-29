package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Legale rappresentante di un'azienda (US-506), uno per azienda; può non coincidere con il referente. Dati personali:
 * {@code toString()} senza valori (da {@link AbstractTsidEntity}), mai nei log.
 */
@Getter
@Setter
@Entity
@Table(name = "company_legal_representative")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyLegalRepresentative extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true, updatable = false)
    @Setter(AccessLevel.NONE)
    private Company company;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "fiscal_code", length = 16)
    private String fiscalCode;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_place", length = 200)
    private String birthPlace;

    @Column(name = "residence", length = 300)
    private String residence;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    public CompanyLegalRepresentative(Company company) {
        this.company = company;
    }
}
