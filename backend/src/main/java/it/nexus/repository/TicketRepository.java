package it.nexus.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByNumber(Long number);

    /**
     * Coda del Call Center: ticket non assegnati negli stati indicati, prima i fast-track e poi per arrivo. Filtri
     * facoltativi (null = nessun filtro) per progetto e zona di residenza del candidato.
     */
    @Query("""
            select t from Ticket t
            join fetch t.candidate c
            join fetch c.residenceZone z
            join fetch t.project p
            join fetch t.tutor
            left join fetch t.requestedJobCategory
            where t.status in :statuses
              and t.assignedCcOperator is null
              and (:projectId is null or p.id = :projectId)
              and (:zoneId is null or z.id = :zoneId)
            order by t.fastTrack desc, t.createdAt asc, t.number asc
            """)
    List<Ticket> findQueue(@Param("statuses") Collection<TicketStatus> statuses, @Param("projectId") Long projectId,
            @Param("zoneId") Long zoneId);
}
