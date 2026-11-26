package it.nexus.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.ClosureReport;

@Repository
public interface ClosureReportRepository extends JpaRepository<ClosureReport, Long> {

    Optional<ClosureReport> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);

    /** Valutazioni di un'azienda (US-1104): i report con i voti, dal tirocinio finito più di recente. */
    @Query(value = """
            select r from ClosureReport r
            where r.company.id = :companyId and r.companyInteraction is not null
            order by r.actualEndDate desc nulls last, r.createdAt desc, r.id desc
            """, countQuery = """
            select count(r) from ClosureReport r
            where r.company.id = :companyId and r.companyInteraction is not null
            """)
    Page<ClosureReport> findCompanyReviews(@Param("companyId") Long companyId, Pageable page);

    /**
     * Per ogni azienda indicata con almeno una valutazione: id, media delle medie dei tre voti e numero delle
     * valutazioni (US-1104).
     */
    @Query("""
            select r.company.id, avg((r.companyInteraction + r.companyWelcome + r.companyTraining) / 3.0), count(r)
            from ClosureReport r
            where r.company.id in :companyIds and r.companyInteraction is not null
            group by r.company.id
            """)
    List<Object[]> companyReviewSummaries(@Param("companyIds") Collection<Long> companyIds);
}
