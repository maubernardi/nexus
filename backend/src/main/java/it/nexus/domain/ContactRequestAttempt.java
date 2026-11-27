package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import it.nexus.domain.enumeration.ContactOutcome;
import it.nexus.domain.id.TsidId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Tentativo di contatto di una richiesta (US-512): data, esito e nota facoltativa per il Tutor. Non si modifica; chi
 * l'ha segnato e quando sono {@code createdBy}/{@code createdAt}. La nota può contenere dati personali: mai nei log né
 * nell'audit.
 */
@Getter
@Entity
@Immutable
@Table(name = "contact_request_attempt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContactRequestAttempt extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_request_id", nullable = false, updatable = false)
    private ContactRequest contactRequest;

    @Column(name = "attempt_date", nullable = false, updatable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, updatable = false, length = 20)
    private ContactOutcome outcome;

    @Column(name = "note", updatable = false, length = 500)
    private String note;

    public ContactRequestAttempt(ContactRequest contactRequest, LocalDate date, ContactOutcome outcome, String note) {
        this.contactRequest = contactRequest;
        this.date = date;
        this.outcome = outcome;
        this.note = note;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((ContactRequestAttempt) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ContactRequestAttempt[id=" + id + "]";
    }
}
