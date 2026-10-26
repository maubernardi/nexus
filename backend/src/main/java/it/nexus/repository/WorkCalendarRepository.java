package it.nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.WorkCalendar;

@Repository
public interface WorkCalendarRepository extends JpaRepository<WorkCalendar, Short> {
}
