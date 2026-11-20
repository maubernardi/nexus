package it.nexus.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    /** Segnalazioni assegnate alla mansione (US-605), con il tipo, dal numero più basso. */
    @Query("""
            select t from Ticket t left join fetch t.internshipType
            where t.jobSlot.id = :jobSlotId and t.jobSlotHeld = true
            order by t.number
            """)
    List<Ticket> findHolders(@Param("jobSlotId") Long jobSlotId);

    /** Segnalazioni assegnate a una delle mansioni (US-605), con il tipo: le assegnazioni di un elenco di mansioni. */
    @Query("""
            select t from Ticket t join fetch t.jobSlot left join fetch t.internshipType
            where t.jobSlot.id in :jobSlotIds and t.jobSlotHeld = true
            order by t.number
            """)
    List<Ticket> findHolders(@Param("jobSlotIds") Collection<Long> jobSlotIds);


    Optional<Ticket> findByNumber(Long number);

    /** Segnalazioni del Tutor non chiuse, per lo spostamento (US-111). */
    List<Ticket> findByTutorIdAndStatusNotOrderByNumberAsc(Long tutorId, TicketStatus closed);

    /** Segnalazioni del progetto non chiuse, per il Coordinatore (US-112). */
    List<Ticket> findByProjectIdAndStatusNotOrderByNumberAsc(Long projectId, TicketStatus closed);

    /** Segnalazioni aperte di cui l'utente è il Tutor: bloccano disattivazione e cambio di ruolo (US-105). */
    long countByTutorIdAndStatusNot(Long tutorId, TicketStatus closed);

    /** Ticket aperti in carico all'operatore: tornano in coda quando esce (US-105). */
    List<Ticket> findByAssignedCcOperatorIdAndStatusNot(Long operatorId, TicketStatus closed);

    /** Segnalazioni non concluse dei beneficiari indicati (al più una per beneficiario). */
    List<Ticket> findByBeneficiaryIdInAndStatusNot(Collection<Long> beneficiaryIds, TicketStatus closed);

    /** Vero se il beneficiario ha almeno una segnalazione (anche conclusa) in uno dei progetti indicati (US-204). */
    boolean existsByBeneficiaryIdAndProjectIdIn(Long beneficiaryId, Collection<Long> projectIds);

    /** Proposte accettate con il timer attivo e il promemoria non ancora inviato (US-704). */
    List<Ticket> findByStatusAndTimerStartedAtIsNotNullAndTimerReminderSentAtIsNull(TicketStatus status);

    /** Proposte accettate con il timer scaduto all'istante indicato (US-705). */
    List<Ticket> findByStatusAndTimerDeadlineAtLessThanEqual(TicketStatus status, Instant at);

    Optional<Ticket> findFirstByBeneficiaryIdAndStatusNot(Long beneficiaryId, TicketStatus closed);

    /** Candidatura in valutazione sull'annuncio: al più una ({@code uq_ticket_fast_track_board_post}, US-306). */
    Optional<Ticket> findFirstByBoardPostIdAndFastTrackTrue(Long boardPostId);

    /** Candidature in valutazione sugli annunci indicati (US-306). */
    List<Ticket> findByBoardPostIdInAndFastTrackTrue(Collection<Long> boardPostIds);
}
