package it.nexus.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.ContactRequest;
import it.nexus.domain.enumeration.ContactRequestStatus;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {

    /** «Le mie richieste» del Tutor, dalla più recente (US-507). */
    @EntityGraph(attributePaths = {"company", "zone", "businessCategory", "assignedOperator"})
    List<ContactRequest> findByRequestedByIdOrderByCreatedAtDescIdDesc(Long tutorId);

    /**
     * Elenco del Call Center (US-507): in cima quelle da prendere in carico (PO, 27/11), poi le altre; dentro ognuna
     * dalla più vecchia.
     */
    @EntityGraph(attributePaths = {"requestedBy", "company", "zone", "businessCategory", "assignedOperator"})
    @Query("""
            select r from ContactRequest r
            where r.status in :statuses
            order by case when r.status = it.nexus.domain.enumeration.ContactRequestStatus.APERTA then 0 else 1 end,
                     r.createdAt, r.id
            """)
    List<ContactRequest> queue(@Param("statuses") Collection<ContactRequestStatus> statuses);

    /** Richieste ancora da prendere in carico: il numero nel menu del Call Center. */
    long countByStatus(ContactRequestStatus status);
}
