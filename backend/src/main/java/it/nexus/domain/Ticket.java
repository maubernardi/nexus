package it.nexus.domain;

import java.io.Serial;
import java.time.Instant;

import org.hibernate.annotations.Generated;

import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;
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
import lombok.Setter;

/**
 * Segnalazione / percorso di tirocinio. {@code number} è il progressivo leggibile assegnato dal database.
 * La mansione richiesta è una tipologia del catalogo oppure un testo libero (mai entrambi); azienda e zona della
 * proposta si ricavano da {@code jobSlot}. Le transizioni di stato sono responsabilità della macchina a stati.
 */
@Getter
@Setter
@Entity
@Table(name = "ticket")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Generated
    @Column(name = "number", nullable = false, unique = true, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Long number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private AppUser tutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TicketType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private TicketStatus status;

    @Column(name = "is_fast_track", nullable = false)
    private boolean fastTrack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_job_category_id")
    private JobCategory requestedJobCategory;

    @Column(name = "requested_job_free_text", length = 200)
    private String requestedJobFreeText;

    /** Post di bacheca da cui è nata la segnalazione (obbligatorio per {@code SPECIAL}). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_post_id")
    private BoardPost boardPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_cc_operator_id")
    private AppUser assignedCcOperator;

    /** Mansione abbinata dal Call Center. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_slot_id")
    private JobSlot jobSlot;

    @Column(name = "timer_started_at")
    private Instant timerStartedAt;

    @Column(name = "timer_reminder_sent_at")
    private Instant timerReminderSentAt;

    @Column(name = "timer_deadline_at")
    private Instant timerDeadlineAt;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    @Override
    public String toString() {
        return "Ticket[id=" + externalId() + ", number=" + number + ", status=" + status + "]";
    }
}
