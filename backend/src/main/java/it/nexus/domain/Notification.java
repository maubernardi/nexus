package it.nexus.domain;

import java.io.Serial;
import java.time.Instant;

import org.hibernate.Hibernate;

import it.nexus.domain.enumeration.NotificationType;
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

/** Notifica in app (US-1201): destinatario, tipo e segnalazione collegata; nessun dato personale. */
@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false, updatable = false)
    private AppUser recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, length = 40)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", updatable = false)
    private Ticket ticket;

    @Column(name = "read_at")
    private Instant readAt;

    public Notification(AppUser recipient, NotificationType type, Ticket ticket) {
        this.recipient = recipient;
        this.type = type;
        this.ticket = ticket;
    }

    public void markRead(Instant at) {
        if (readAt == null) {
            readAt = at;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((Notification) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Notification[id=" + id + ", type=" + type + "]";
    }
}
