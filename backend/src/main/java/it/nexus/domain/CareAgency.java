package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

import it.nexus.domain.enumeration.CareAgencyKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ente che ha in carico i beneficiari dei tirocini di inclusione (US-115), con i dati per la convenzione e il progetto
 * personalizzato. I dati sono facoltativi; legale rappresentante e referente sono persone: niente nei log.
 */
@Getter
@Setter
@Entity
@Table(name = "care_agency")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareAgency extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 30)
    private CareAgencyKind kind = CareAgencyKind.ALTRO;

    @Column(name = "fiscal_code", length = 16)
    private String fiscalCode;

    @Column(name = "legal_address", length = 300)
    private String legalAddress;

    @Column(name = "representative_name", length = 200)
    private String representativeName;

    @Column(name = "representative_role", length = 200)
    private String representativeRole;

    @Column(name = "representative_birth_place", length = 200)
    private String representativeBirthPlace;

    @Column(name = "representative_birth_date")
    private LocalDate representativeBirthDate;

    @Column(name = "referent_name", length = 200)
    private String referentName;

    @Column(name = "referent_phone", length = 30)
    private String referentPhone;

    @Column(name = "referent_email", length = 254)
    private String referentEmail;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /** Nuovo ente del catalogo: i dati li aggiunge l'ADMIN dopo. */
    public CareAgency(String code, String name) {
        setCode(code);
        setName(name);
    }
}
