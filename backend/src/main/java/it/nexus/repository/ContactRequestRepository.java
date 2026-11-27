package it.nexus.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.ContactRequest;
import it.nexus.domain.enumeration.ContactRequestStatus;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {

    /** «Le mie richieste» del Tutor, dalla più recente (US-507). */
    @EntityGraph(attributePaths = {"company", "company.zone", "company.businessCategory", "zone", "businessCategory",
            "assignedOperator"})
    List<ContactRequest> findByRequestedByIdOrderByCreatedAtDescIdDesc(Long tutorId);

    /** Elenco del Call Center, dalla più vecchia (US-507). */
    @EntityGraph(attributePaths = {"requestedBy", "company", "company.zone", "company.businessCategory", "zone",
            "businessCategory", "assignedOperator"})
    List<ContactRequest> findByStatusInOrderByCreatedAtAscIdAsc(Collection<ContactRequestStatus> statuses);

    /** Richieste ancora da prendere in carico: il numero nel menu del Call Center. */
    long countByStatus(ContactRequestStatus status);
}
