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
 * Referente dell'azienda (US-510), nella posizione indicata: riga sostituita in blocco quando i referenti cambiano, mai
 * modificata. Serve almeno il nome o il ruolo ({@code ck_company_contact_name_or_role}). Dati personali:
 * {@code toString()} senza valori.
 */
@Getter
@Entity
@Table(name = "company_contact")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyContact extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, updatable = false)
    private Company company;

    @Column(name = "position", nullable = false, updatable = false)
    private int position;

    @Column(name = "name", updatable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", updatable = false)
    private ContactRole role;

    @Column(name = "phone", updatable = false, length = 30)
    private String phone;

    @Column(name = "mobile", updatable = false, length = 30)
    private String mobile;

    @Column(name = "email", updatable = false, length = 254)
    private String email;

    CompanyContact(Company company, int position, String name, ContactRole role, String phone, String mobile,
            String email) {
        this.company = company;
        this.position = position;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.mobile = mobile;
        this.email = email;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((CompanyContact) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CompanyContact[id=" + id + "]";
    }
}
