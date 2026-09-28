package it.nexus.domain;

import java.io.Serial;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.nexus.domain.enumeration.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Cambio di stato di un ticket; istante e autore sono {@code createdAt}/{@code createdBy}. */
@Getter
@Entity
@Table(name = "ticket_status_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketStatusHistory extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 40)
    private TicketStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 40)
    private TicketStatus toStatus;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "note")
    private String note;

    public TicketStatusHistory(Ticket ticket, TicketStatus fromStatus, TicketStatus toStatus, String note) {
        this.ticket = ticket;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.note = note;
    }
}
