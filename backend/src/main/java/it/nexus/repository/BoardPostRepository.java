package it.nexus.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.BoardPost;
import it.nexus.domain.enumeration.BoardPostStatus;

@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    Optional<BoardPost> findByNumber(Long number);

    /** Annunci in uno stato, dal più recente, con mansione, azienda, progetto e segnalazione d'origine. */
    @EntityGraph(attributePaths = {"jobSlot.company", "jobSlot.jobCategory", "jobSlot.zone", "project", "createdFromTicket"})
    List<BoardPost> findByStatusOrderByNumberDesc(BoardPostStatus status);

    /** Annunci attivi (bozza o pubblicato) delle mansioni indicate: al più uno per mansione. */
    List<BoardPost> findByJobSlot_IdInAndStatusIn(Collection<Long> jobSlotIds, Collection<BoardPostStatus> statuses);

    /**
     * Bacheca del Tutor (US-901): annunci pubblicati per tutti o riservati ai suoi progetti, dal più recente; filtri
     * facoltativi per zona e tipologia della mansione.
     */
    @Query("""
            select p from BoardPost p
            join fetch p.jobSlot s join fetch s.jobCategory join fetch s.zone left join fetch p.project
            where p.status = it.nexus.domain.enumeration.BoardPostStatus.PUBLISHED
              and (p.project is null or p.project.id in :projectIds)
              and (:zoneId is null or s.zone.id = :zoneId)
              and (:jobCategoryId is null or s.jobCategory.id = :jobCategoryId)
            order by p.publishedAt desc, p.number desc
            """)
    List<BoardPost> findVisibleToTutor(@Param("projectIds") Collection<Long> projectIds, @Param("zoneId") Long zoneId,
            @Param("jobCategoryId") Long jobCategoryId);

    /** Ultimo annuncio nato dal rilancio della segnalazione (US-803). */
    Optional<BoardPost> findFirstByCreatedFromTicket_IdOrderByNumberDesc(Long ticketId);

    Optional<BoardPost> findFirstByJobSlot_IdAndStatusIn(Long jobSlotId, Collection<BoardPostStatus> statuses);
}
