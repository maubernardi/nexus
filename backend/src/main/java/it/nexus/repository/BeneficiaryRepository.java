package it.nexus.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Beneficiary;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    /**
     * Ultima attività di un beneficiario (US-1303): la sua scheda, le sue segnalazioni e le schede dei tirocini. Il segno
     * «da anonimizzare» si scrive con un update diretto, che non tocca {@code updated_at}: non conta come attività.
     */
    String LAST_ACTIVITY = """
            GREATEST(b.updated_at,
                COALESCE((SELECT max(t.updated_at) FROM ticket t WHERE t.beneficiary_id = b.id), b.updated_at),
                COALESCE((SELECT max(i.updated_at) FROM internship i JOIN ticket t ON t.id = i.ticket_id
                          WHERE t.beneficiary_id = b.id), b.updated_at))
            """;

    @EntityGraph(attributePaths = "residenceZone")
    List<Beneficiary> findByOwnerTutorIdOrderByLastNameAscFirstNameAsc(Long ownerTutorId);

    /** Tutti, per l'ADMIN (US-1303). */
    @EntityGraph(attributePaths = {"residenceZone", "ownerTutor"})
    List<Beneficiary> findAllByOrderByLastNameAscFirstNameAsc();

    /** Da segnare: né anonimizzati, né tenuti, né già segnati, senza attività da prima di {@code threshold}. */
    @Query(value = "SELECT b.id FROM beneficiary b WHERE b.anonymized_at IS NULL AND b.kept_reason IS NULL"
            + " AND b.anonymization_due_since IS NULL AND " + LAST_ACTIVITY + " < :threshold", nativeQuery = true)
    List<Long> findIdsInactiveSince(@Param("threshold") Instant threshold);

    /** Segnati che hanno avuto attività dopo il segno: non sono più da anonimizzare. */
    @Query(value = "SELECT b.id FROM beneficiary b WHERE b.anonymization_due_since IS NOT NULL AND "
            + LAST_ACTIVITY + " > b.anonymization_due_since", nativeQuery = true)
    List<Long> findIdsActiveAfterDue();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Beneficiary b set b.anonymizationDueSince = :due where b.id in :ids")
    int setAnonymizationDue(@Param("ids") Collection<Long> ids, @Param("due") Instant due);
}
