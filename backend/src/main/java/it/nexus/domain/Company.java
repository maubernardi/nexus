package it.nexus.domain;

import java.io.Serial;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    /** Partita IVA: 11 cifre. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "vat_code", nullable = false, unique = true, length = 11)
    private String vatCode;

    @Column(name = "legal_address", length = 300)
    private String legalAddress;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 254)
    private String email;

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
}
