package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.JobCategory;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    Optional<JobCategory> findByCode(String code);

    List<JobCategory> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-106): prima le attive, poi per nome. */
    List<JobCategory> findAllByOrderByActiveDescNameAsc();

    /** Tipologia attiva con lo stesso nome, senza distinguere maiuscole e minuscole (US-302). */
    Optional<JobCategory> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
