package it.nexus.domain;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.nexus.domain.enumeration.CompanyMissingData;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Azienda ospitante. Si disattiva, non si cancella (ha storico e audit trail). */
@Getter
@Setter
@Entity
@Table(name = "company")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Partita IVA: 11 cifre, unica tranne il segnaposto {@link CompanyPlaceholders#VAT_CODE} (US-508). */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "vat_code", nullable = false, length = 11)
    private String vatCode;

    /** Indirizzo della sede legale (via e numero civico); il comune è a parte (US-1007). */
    @Column(name = "legal_address", length = 300)
    private String legalAddress;

    /** Versione del comune della sede legale scelta dall'elenco ufficiale (US-1007). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_municipality_id")
    private Municipality legalMunicipality;

    /** Categoria merceologica dal catalogo dell'ADMIN (US-509); facoltativa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_category_id")
    private BusinessCategory businessCategory;

    /** Zona del catalogo (US-511), facoltativa: la sceglie il Call Center, il Tutor la usa come filtro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    /** Descrizione dell'attività, testo libero (US-509): in Access il campo «Paese». */
    @Column(name = "activity", length = 200)
    private String activity;

    /** Sede operativa (US-510): indirizzo e versione del comune, come la sede legale. */
    @Column(name = "operating_address", length = 300)
    private String operatingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operating_municipality_id")
    private Municipality operatingMunicipality;

    // la colonna contact_person resta per il ripristino della release precedente (A16): V47 l'ha copiata nei referenti

    @Column(name = "phone", length = 30)
    private String phone;

    /** Cellulare, fax e seconda email dell'azienda (US-510). */
    @Column(name = "mobile", length = 30)
    private String mobile;

    @Column(name = "fax", length = 30)
    private String fax;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "secondary_email", length = 254)
    private String secondaryEmail;

    /**
     * Referenti (US-510), in ordine: sostituiti in blocco quando cambiano. Nell'elenco delle aziende arrivano a lotti,
     * senza moltiplicare le righe della ricerca.
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    @BatchSize(size = 100)
    @Setter(AccessLevel.NONE)
    private List<CompanyContact> contacts = new ArrayList<>();

    /** Affidabilità per il Call Center, da 1 a 4 stelle (US-514); {@code null} = non valutata. */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "reliability")
    private Integer reliability;

    /** Interesse al progetto, uno o due pollici alzati (US-514); {@code null} = non valutato. */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "project_interest")
    private Integer projectInterest;

    /** Nave scuola (US-515): adatta a un primo tirocinio; la segnano operatori e ADMIN. */
    @Column(name = "training_ship", nullable = false)
    private boolean trainingShip;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    /** Nuova azienda (US-501); gli altri dati si impostano con i setter. */
    public Company(String name, String vatCode) {
        this.name = name;
        this.vatCode = vatCode;
    }

    /** Aggiunge un referente in fondo all'elenco. */
    public void addContact(String name, ContactRole role, String phone, String mobile, String email) {
        contacts.add(new CompanyContact(this, contacts.size(), name, role, phone, mobile, email));
    }

    /** Dati arrivati vuoti dal database attuale e ancora da completare (US-508), in ordine. */
    public List<CompanyMissingData> missingData() {
        List<CompanyMissingData> missing = new ArrayList<>();
        if (CompanyPlaceholders.isName(name)) {
            missing.add(CompanyMissingData.NAME);
        }
        if (CompanyPlaceholders.isVatCode(vatCode)) {
            missing.add(CompanyMissingData.VAT_CODE);
        }
        return List.copyOf(missing);
    }
}
