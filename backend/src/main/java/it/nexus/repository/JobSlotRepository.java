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

    /**
     * Mansioni proponibili per un ticket (US-601): libere, a disposizione, di aziende attive e non escluse per quel
     * ticket; filtri facoltativi per zona e tipologia (null = tutte).
     */
    @Query("""
            select j from JobSlot j
            join fetch j.company c
            join fetch j.jobCategory jc
            join fetch j.zone z
            where j.status = it.nexus.domain.enumeration.JobSlotStatus.LIBERA
              and j.active = true
              and c.active = true
              and (:zoneId is null or z.id = :zoneId)
              and (:jobCategoryId is null or jc.id = :jobCategoryId)
              and not exists (select 1 from TicketCompanyBlacklist b where b.ticket.id = :ticketId and b.company = c)
            order by lower(c.name), lower(j.title), j.id
            """)
    List<JobSlot> findCompatible(@Param("ticketId") Long ticketId, @Param("zoneId") Long zoneId,
            @Param("jobCategoryId") Long jobCategoryId);
}
