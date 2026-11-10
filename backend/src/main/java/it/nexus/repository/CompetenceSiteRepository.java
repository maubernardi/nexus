package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.CompetenceSite;

@Repository
public interface CompetenceSiteRepository extends JpaRepository<CompetenceSite, Long> {

    List<CompetenceSite> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-115): prima le voci attive, poi per nome. */
    List<CompetenceSite> findAllByOrderByActiveDescNameAsc();

    Optional<CompetenceSite> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
