package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.NoHireReason;

@Repository
public interface NoHireReasonRepository extends JpaRepository<NoHireReason, Long> {

    List<NoHireReason> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-109): prima le voci attive, poi per nome. */
    List<NoHireReason> findAllByOrderByActiveDescNameAsc();

    /** Voce attiva con lo stesso nome ({@code uq_no_hire_reason_active_name}). */
    Optional<NoHireReason> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
