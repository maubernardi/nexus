package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.CareAgency;

@Repository
public interface CareAgencyRepository extends JpaRepository<CareAgency, Long> {

    List<CareAgency> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-115): prima le voci attive, poi per nome. */
    List<CareAgency> findAllByOrderByActiveDescNameAsc();

    Optional<CareAgency> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
