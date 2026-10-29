package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Internship;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    Optional<Internship> findByTicketId(Long ticketId);

    /** Una scheda che ha scelto il codice ATECO: finché c'è, il codice non si toglie dall'azienda. */
    Optional<Internship> findFirstByAtecoCodeId(Long atecoCodeId);
}
