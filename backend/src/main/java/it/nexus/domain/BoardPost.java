package it.nexus.domain;

import java.io.Serial;
import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.nexus.domain.enumeration.BoardPostStatus;
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
 * Annuncio di bacheca ({@code #number}). Azienda e zona si ricavano dalla mansione; {@code project == null} significa
 * visibile a tutti. {@code ageMin}/{@code ageMax} esprimono requisiti del programma di finanziamento, non preferenze.
 */
@Getter
@Setter
@Entity
@Table(name = "board_post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardPost extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Generated
    @Column(name = "number", nullable = false, unique = true, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Long number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_slot_id", nullable = false)
    private JobSlot jobSlot;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BoardPostStatus status = BoardPostStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "age_min")
    private Integer ageMin;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "age_max")
    private Integer ageMax;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "weekly_hours")
    private Integer weeklyHours;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "duration_months")
    private Integer durationMonths;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "notes")
    private String notes;

    @Column(name = "published_at")
    private Instant publishedAt;

    /** Quando il post riservato a un progetto è stato reso pubblico a tutti. */
    @Column(name = "made_public_at")
    private Instant madePublicAt;

    /** Ticket da cui è nato il post (rilancio in bacheca, fase 6). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_from_ticket_id")
    private Ticket createdFromTicket;

    @Version
    @Column(name = "version", nullable = false)
    @Setter(AccessLevel.NONE)
    private long version;

    @Override
    public String toString() {
        return "BoardPost[id=" + getId() + ", number=" + number + ", status=" + status + "]";
    }
}
