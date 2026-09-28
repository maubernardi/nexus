package it.nexus.domain;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Azienda esclusa per un ticket (da non riproporre), con motivo. */
@Getter
@Entity
@Table(name = "ticket_company_blacklist")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketCompanyBlacklist extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private Id id;

    @MapsId("ticketId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @MapsId("companyId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "reason", length = 500)
    private String reason;

    public TicketCompanyBlacklist(Ticket ticket, Company company, String reason) {
        this.id = new Id(ticket.getId(), company.getId());
        this.ticket = ticket;
        this.company = company;
        this.reason = reason;
    }

    @Getter
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Id implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Column(name = "ticket_id")
        private Long ticketId;

        @Column(name = "company_id")
        private Long companyId;

        public Id(Long ticketId, Long companyId) {
            this.ticketId = ticketId;
            this.companyId = companyId;
        }
    }
}
