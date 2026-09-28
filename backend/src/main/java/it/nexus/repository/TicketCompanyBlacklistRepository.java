package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.TicketCompanyBlacklist;

@Repository
public interface TicketCompanyBlacklistRepository extends JpaRepository<TicketCompanyBlacklist, TicketCompanyBlacklist.Id> {

    List<TicketCompanyBlacklist> findByIdTicketId(Long ticketId);
}
