package it.nexus.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByNumber(Long number);

    /** Segnalazioni non concluse dei beneficiari indicati (al più una per beneficiario). */
    List<Ticket> findByBeneficiaryIdInAndStatusNot(Collection<Long> beneficiaryIds, TicketStatus closed);

    /** Vero se il beneficiario ha almeno una segnalazione (anche conclusa) in uno dei progetti indicati (US-204). */
    boolean existsByBeneficiaryIdAndProjectIdIn(Long beneficiaryId, Collection<Long> projectIds);

    /** Proposte accettate con il timer attivo e il promemoria non ancora inviato (US-704). */
    List<Ticket> findByStatusAndTimerStartedAtIsNotNullAndTimerReminderSentAtIsNull(TicketStatus status);

    /** Proposte accettate con il timer scaduto all'istante indicato (US-705). */
    List<Ticket> findByStatusAndTimerDeadlineAtLessThanEqual(TicketStatus status, Instant at);

    Optional<Ticket> findFirstByBeneficiaryIdAndStatusNot(Long beneficiaryId, TicketStatus closed);
}
