package it.nexus.domain;

import java.io.Serial;
import java.time.Instant;

import org.hibernate.annotations.Generated;

import it.nexus.domain.enumeration.ContactOutcome;
import it.nexus.domain.enumeration.ContactRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Richiesta di contatto di un'azienda dal Tutor al Call Center (US-507): generale, senza beneficiario né segnalazione,
 * solo per un'azienda non ancora in anagrafica (PO, 27/11): nome e zona, categoria facoltativa. {@code company} la
 * collega l'operatore alla chiusura (US-512). {@code number} è il progressivo leggibile assegnato dal database.
 */
@Getter
@Entity
@Table(name = "contact_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContactRequest extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Generated
    @Column(name = "number", nullable = false, unique = true, insertable = false, updatable = false)
    private Long number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false, updatable = false)
    private AppUser requestedBy;

    /** Azienda dell'anagrafica collegata alla chiusura (US-512); assente fino ad allora. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    /** L'azienda come l'ha indicata il Tutor: nome, zona e categoria facoltativa. */
    @Column(name = "company_name", nullable = false, updatable = false, length = 200)
    private String companyName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false, updatable = false)
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_category_id", updatable = false)
    private BusinessCategory businessCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContactRequestStatus status = ContactRequestStatus.APERTA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_operator_id")
    private AppUser assignedOperator;

    @Column(name = "taken_at")
    private Instant takenAt;

    /** Chiusura (US-512): esito finale, nota per il Tutor e istante; assenti finché è aperta o in carico. */
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 20)
    private ContactOutcome outcome;

    @Column(name = "closing_note", length = 500)
    private String closingNote;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /** Richiesta per un'azienda non ancora in anagrafica. */
    public static ContactRequest forNewCompany(AppUser tutor, String name, Zone zone, BusinessCategory category) {
        ContactRequest request = new ContactRequest();
        request.requestedBy = tutor;
        request.companyName = name;
        request.zone = zone;
        request.businessCategory = category;
        return request;
    }

    /** Presa in carico da un operatore (US-507); solo da aperta. */
    public void take(AppUser operator, Instant at) {
        if (status != ContactRequestStatus.APERTA) {
            throw new IllegalStateException("Richiesta non aperta");
        }
        status = ContactRequestStatus.IN_CARICO;
        assignedOperator = operator;
        takenAt = at;
    }

    /**
     * Chiusura a mano (US-512), solo da in carico: esito finale, nota per il Tutor e, se c'è, l'azienda dell'anagrafica.
     * Una richiesta chiusa non si riapre.
     */
    public void close(ContactOutcome finalOutcome, String note, Company linkedCompany, Instant at) {
        if (status != ContactRequestStatus.IN_CARICO) {
            throw new IllegalStateException("Richiesta non in carico");
        }
        status = ContactRequestStatus.CHIUSA;
        outcome = finalOutcome;
        closingNote = note;
        company = linkedCompany;
        closedAt = at;
    }

    @Override
    public String toString() {
        return "ContactRequest[id=" + externalId() + ", number=" + number + ", status=" + status + "]";
    }
}
