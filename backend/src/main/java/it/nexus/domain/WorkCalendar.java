package it.nexus.domain;

import java.io.Serial;
import java.time.MonthDay;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Calendario unico dei giorni lavorativi (US-107): una sola riga, creata dalla migrazione ({@link #SINGLETON_ID}).
 * Contiene il patrono; le chiusure sono in {@link CalendarClosure}, le festività nazionali nel codice (US-703).
 */
@Getter
@Entity
@Table(name = "work_calendar")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkCalendar extends AbstractAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final short SINGLETON_ID = 1;

    @Id
    private Short id;

    @Column(name = "patron_month", nullable = false)
    private short patronMonth;

    @Column(name = "patron_day", nullable = false)
    private short patronDay;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public MonthDay getPatron() {
        return MonthDay.of(patronMonth, patronDay);
    }

    public void setPatron(MonthDay patron) {
        this.patronMonth = (short) patron.getMonthValue();
        this.patronDay = (short) patron.getDayOfMonth();
    }

    @Override
    public String toString() {
        return "WorkCalendar[id=" + id + "]";
    }
}
