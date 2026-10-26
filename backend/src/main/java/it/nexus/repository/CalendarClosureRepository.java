package it.nexus.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.CalendarClosure;

@Repository
public interface CalendarClosureRepository extends JpaRepository<CalendarClosure, Long> {

    List<CalendarClosure> findAllByOrderByStartDateAscEndDateAsc();

    /** Chiusure che toccano l'intervallo indicato (per il calcolo dei giorni lavorativi, US-703). */
    List<CalendarClosure> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate to, LocalDate from);

    /** Chiusure in corso o future rispetto alla data indicata (US-703). */
    List<CalendarClosure> findByEndDateGreaterThanEqual(LocalDate from);
}
