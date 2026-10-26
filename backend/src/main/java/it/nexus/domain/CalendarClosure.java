package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Chiusura della cooperativa nel calendario unico (US-107): un giorno o un periodo, estremi compresi. */
@Getter
@Entity
@Table(name = "calendar_closure")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarClosure extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    public CalendarClosure(LocalDate startDate, LocalDate endDate, String description) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public boolean includes(LocalDate day) {
        return !day.isBefore(startDate) && !day.isAfter(endDate);
    }
}
