package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.ClosureReason;

@Repository
public interface ClosureReasonRepository extends JpaRepository<ClosureReason, Long> {

    List<ClosureReason> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-1103): prima le voci attive, poi per nome. */
    List<ClosureReason> findAllByOrderByActiveDescNameAsc();

    /** Voce attiva con lo stesso nome ({@code uq_closure_reason_active_name}). */
    Optional<ClosureReason> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);

    Optional<ClosureReason> findByCode(String code);
}
