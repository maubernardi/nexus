package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.CompetenceSite;

@Repository
public interface CompetenceSiteRepository extends JpaRepository<CompetenceSite, Long> {

    List<CompetenceSite> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-115): prima le voci attive, poi per nome. */
    List<CompetenceSite> findAllByOrderByActiveDescNameAsc();

    Optional<CompetenceSite> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);

    /** Sedi attive di un progetto (US-308), per nome. */
    @Query("select s from CompetenceSite s join s.projects p where p.id = :projectId and s.active = true "
            + "order by s.name")
    List<CompetenceSite> findActiveForProject(@Param("projectId") Long projectId);

    /** Il progetto ha almeno una sede attiva: senza, la correzione non avrebbe nulla da proporre (US-308). */
    @Query("select count(s) > 0 from CompetenceSite s join s.projects p where p.id = :projectId and s.active = true")
    boolean existsActiveForProject(@Param("projectId") Long projectId);
}
