package it.nexus.domain;

import java.io.Serial;

import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
 * Giorno della griglia oraria di un tirocinio (US-1002): fasce scelte e minuti, nulli = fasce intere. Riga sostituita
 * in blocco a ogni salvataggio della scheda, mai modificata.
 */
@Getter
@Entity
@Table(name = "internship_schedule_day")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InternshipScheduleDay extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_id", nullable = false, updatable = false)
    private Internship internship;

    /** 1 = lunedì … 7 = domenica (ISO). */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "day_of_week", nullable = false, updatable = false)
    private Integer dayOfWeek;

    @Column(name = "first_slot", nullable = false, updatable = false)
    private boolean firstSlot;

    @Column(name = "second_slot", nullable = false, updatable = false)
    private boolean secondSlot;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "minutes", updatable = false)
    private Integer minutes;

    InternshipScheduleDay(Internship internship, int dayOfWeek, boolean firstSlot, boolean secondSlot,
            Integer minutes) {
        this.internship = internship;
        this.dayOfWeek = dayOfWeek;
        this.firstSlot = firstSlot;
        this.secondSlot = secondSlot;
        this.minutes = minutes;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((InternshipScheduleDay) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "InternshipScheduleDay[id=" + id + "]";
    }
}
