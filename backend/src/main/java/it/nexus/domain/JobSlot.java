package it.nexus.domain;

import java.io.Serial;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.nexus.domain.enumeration.JobSlotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mansione offerta da un'azienda. Si assegna a più segnalazioni, al più una per tipo di tirocinio (US-605):
 * l'assegnazione sta sulla segnalazione ({@code job_slot_held}). Stato e {@code blockedByTicket} ne sono il riassunto:
 * {@code BLOCCATA} se e solo se c'è almeno un'assegnazione (vincoli del database). Protetta da blocco ottimistico.
 */
@Getter
@Setter
@Entity
@Table(name = "job_slot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobSlot extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory jobCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobSlotStatus status = JobSlotStatus.LIBERA;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by_ticket_id", unique = true)
    private Ticket blockedByTicket;

    /** Falso = ritirata: l'azienda non la offre più (resta nello storico). */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    /** Nuova mansione libera di un'azienda (US-502). */
    public JobSlot(Company company, JobCategory jobCategory, Zone zone, String title) {
        this.company = company;
        this.jobCategory = jobCategory;
        this.zone = zone;
        this.title = title;
    }

    /**
     * Assegna la mansione alla segnalazione, per il suo tipo di tirocinio (US-605), e aggiorna il riassunto se è la
     * prima o ha il numero più basso. Nel servizio l'assegnazione passa da {@code JobSlotAssignments}.
     */
    public void blockFor(Ticket ticket) {
        ticket.setJobSlot(this);
        ticket.setJobSlotHeld(true);
        if (blockedByTicket == null || ticket.getNumber() != null && blockedByTicket.getNumber() != null
                && ticket.getNumber() < blockedByTicket.getNumber()) {
            summarize(ticket);
        }
    }

    /** Riassunto senza assegnazioni: la mansione è libera. */
    public void release() {
        summarize(null);
    }

    /**
     * Riassunto delle assegnazioni (US-605): {@code BLOCCATA} con almeno una, riferita alla segnalazione con il numero
     * più basso; {@code LIBERA} senza. La release precedente (A16) legge la mansione da qui.
     */
    public void summarize(Ticket first) {
        this.status = first == null ? JobSlotStatus.LIBERA : JobSlotStatus.BLOCCATA;
        this.blockedByTicket = first;
    }
}
