package it.nexus.domain;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.nexus.domain.enumeration.EducationLevel;
import it.nexus.domain.enumeration.Gender;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.TransportMode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Beneficiario (tirocinante). Contiene dati personali e di categoria particolare ({@code hasLaw68}, {@code constraints}):
 * protetti dal controllo degli accessi, mai nei log. Dopo l'anonimizzazione nome e cognome possono essere nulli.
 */
@Getter
@Setter
@Entity
@Table(name = "beneficiary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Beneficiary extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_tutor_id", nullable = false)
    private AppUser ownerTutor;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "birth_year", nullable = false)
    private Integer birthYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    /** ISO 3166-1 alpha-2. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "nationality", length = 2)
    private String nationality;

    /** ISO 3166-1 alpha-2. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "citizenship", length = 2)
    private String citizenship;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "residence_zone_id", nullable = false)
    private Zone residenceZone;

    @Column(name = "has_driving_license", nullable = false)
    private boolean hasDrivingLicense;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "license_types", nullable = false, length = 5)
    private LicenseType[] licenseTypes = new LicenseType[0];

    @Column(name = "has_vehicle", nullable = false)
    private boolean hasVehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", length = 20)
    private TransportMode transportMode;

    /** Iscrizione al collocamento mirato (L. 68/99): dato di categoria particolare. */
    @Column(name = "has_law68", nullable = false)
    private boolean hasLaw68;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 30)
    private EducationLevel educationLevel;

    /** Vincoli (barriere, allergie…): può contenere dati sanitari. */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "constraints")
    private String constraints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_file_id")
    private StoredFile cvFile;

    @Column(name = "anonymized_at")
    private Instant anonymizedAt;

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BeneficiaryLanguage> languages = new ArrayList<>();

    /** Imposta i tipi di patente e mantiene coerente il flag di possesso (vincolo del database). */
    public void setLicenseTypes(LicenseType... licenseTypes) {
        this.licenseTypes = licenseTypes == null ? new LicenseType[0] : licenseTypes.clone();
        this.hasDrivingLicense = this.licenseTypes.length > 0;
    }

    public LicenseType[] getLicenseTypes() {
        return licenseTypes.clone();
    }

    /** Nuovo beneficiario del Tutor indicato (proprietario). */
    public Beneficiary(AppUser ownerTutor) {
        this.ownerTutor = ownerTutor;
    }

    public void addLanguage(BeneficiaryLanguage language) {
        language.setBeneficiary(this);
        languages.add(language);
    }
}
