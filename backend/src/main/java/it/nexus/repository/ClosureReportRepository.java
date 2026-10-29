package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.ClosureReport;

@Repository
public interface ClosureReportRepository extends JpaRepository<ClosureReport, Long> {

    Optional<ClosureReport> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);
}
