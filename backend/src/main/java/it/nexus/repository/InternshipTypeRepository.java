package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.InternshipType;

@Repository
public interface InternshipTypeRepository extends JpaRepository<InternshipType, Long> {

    List<InternshipType> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-108): prima le voci attive, poi per nome. */
    List<InternshipType> findAllByOrderByActiveDescNameAsc();

    /** Tipo attivo con lo stesso nome ({@code uq_internship_type_active_name}). */
    Optional<InternshipType> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
