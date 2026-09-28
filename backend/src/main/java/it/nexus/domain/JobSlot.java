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
 * Mansione offerta da un'azienda. È {@code BLOCCATA} se e solo se {@code blockedByTicket} è valorizzato; un ticket
 * blocca al massimo una mansione (vincoli del database). Protetta da blocco ottimistico.
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

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    /** Blocca la mansione per il ticket indicato. */
    public void blockFor(Ticket ticket) {
        this.status = JobSlotStatus.BLOCCATA;
        this.blockedByTicket = ticket;
    }

    /** Rende di nuovo libera la mansione. */
    public void release() {
        this.status = JobSlotStatus.LIBERA;
        this.blockedByTicket = null;
    }
}
