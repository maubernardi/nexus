package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.BoardPost;
import it.nexus.domain.enumeration.BoardPostStatus;

@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    Optional<BoardPost> findByNumber(Long number);

    /** Annunci in uno stato, dal più recente, con mansione, azienda, progetto e segnalazione d'origine. */
    @EntityGraph(attributePaths = {"jobSlot.company", "jobSlot.jobCategory", "jobSlot.zone", "project", "createdFromTicket"})
    List<BoardPost> findByStatusOrderByNumberDesc(BoardPostStatus status);
}
