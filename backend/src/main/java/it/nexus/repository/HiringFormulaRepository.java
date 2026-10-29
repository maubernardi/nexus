package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.HiringFormula;

@Repository
public interface HiringFormulaRepository extends JpaRepository<HiringFormula, Long> {

    List<HiringFormula> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-109): prima le voci attive, poi per nome. */
    List<HiringFormula> findAllByOrderByActiveDescNameAsc();

    /** Voce attiva con lo stesso nome ({@code uq_hiring_formula_active_name}). */
    Optional<HiringFormula> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
