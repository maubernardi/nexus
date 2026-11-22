package it.nexus.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Internship;
import it.nexus.domain.enumeration.TicketStatus;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    Optional<Internship> findByTicketId(Long ticketId);

    /** Schede dei tirocini delle segnalazioni del beneficiario, per l'anonimizzazione (US-1303). */
    @Query("select i from Internship i where i.ticket.beneficiary.id = :beneficiaryId")
    List<Internship> findByBeneficiaryId(@Param("beneficiaryId") Long beneficiaryId);

    /** Una scheda che ha scelto il codice ATECO: finché c'è, il codice non si toglie dall'azienda. */
    Optional<Internship> findFirstByAtecoCodeId(Long atecoCodeId);

    /** Tirocini con la segnalazione nello stato indicato, con le proroghe: per gli avvisi di fine (US-1203). */
    @Query("select distinct i from Internship i join fetch i.ticket t left join fetch i.extensions "
            + "where t.status = :status")
    List<Internship> findWithExtensionsByTicketStatus(@Param("status") TicketStatus status);

    /** Come sopra, del solo Tutor indicato: i tirocini finiti da chiudere bloccano le sue nuove segnalazioni. */
    @Query("select distinct i from Internship i join fetch i.ticket t left join fetch i.extensions "
            + "where t.status = :status and t.tutor.id = :tutorId")
    List<Internship> findWithExtensionsByTicketStatusAndTutorId(@Param("status") TicketStatus status,
            @Param("tutorId") Long tutorId);

    /**
     * Segna l'ultimo avviso di fine tirocinio partito (US-1203) senza toccare la versione della scheda: il controllo
     * delle 8 non deve far fallire il salvataggio di un Tutor che la sta modificando.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE internship SET end_reminder_for = :endDate, end_reminder_stage = :stage WHERE id = :id",
            nativeQuery = true)
    int markEndReminder(@Param("id") Long id, @Param("endDate") LocalDate endDate, @Param("stage") int stage);
}
