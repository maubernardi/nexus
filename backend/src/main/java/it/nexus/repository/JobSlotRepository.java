package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.JobSlot;

@Repository
public interface JobSlotRepository extends JpaRepository<JobSlot, Long> {

    Optional<JobSlot> findByBlockedByTicketId(Long ticketId);
}
