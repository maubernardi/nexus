package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.BusinessCategory;

@Repository
public interface BusinessCategoryRepository extends JpaRepository<BusinessCategory, Long> {

    List<BusinessCategory> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-509): prima le voci attive, poi per nome. */
    List<BusinessCategory> findAllByOrderByActiveDescNameAsc();

    /** Voce attiva con lo stesso nome ({@code uq_business_category_active_name}). */
    Optional<BusinessCategory> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
