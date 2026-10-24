package it.nexus.repository;

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

    Optional<Ticket> findFirstByBeneficiaryIdAndStatusNot(Long beneficiaryId, TicketStatus closed);
}
