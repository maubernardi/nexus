package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Internship;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    Optional<Internship> findByTicketId(Long ticketId);

    /** Schede dei tirocini delle segnalazioni del beneficiario, per l'anonimizzazione (US-1303). */
    @Query("select i from Internship i where i.ticket.beneficiary.id = :beneficiaryId")
    List<Internship> findByBeneficiaryId(@Param("beneficiaryId") Long beneficiaryId);

    /** Una scheda che ha scelto il codice ATECO: finché c'è, il codice non si toglie dall'azienda. */
    Optional<Internship> findFirstByAtecoCodeId(Long atecoCodeId);
}
