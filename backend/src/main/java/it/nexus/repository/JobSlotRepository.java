package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.JobSlot;

@Repository
public interface JobSlotRepository extends JpaRepository<JobSlot, Long> {

    Optional<JobSlot> findByBlockedByTicketId(Long ticketId);

    /** Mansioni di un'azienda: prima le attive, poi per titolo. */
    @Query("""
            select j from JobSlot j
            join fetch j.jobCategory
            join fetch j.zone
            left join fetch j.blockedByTicket
            where j.company.id = :companyId
            order by j.active desc, lower(j.title), j.id
            """)
    List<JobSlot> findByCompany(@Param("companyId") Long companyId);
}
