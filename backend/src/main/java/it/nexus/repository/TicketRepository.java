package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByNumber(Long number);
}
