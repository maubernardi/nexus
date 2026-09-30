package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByCode(String code);

    List<Project> findByActiveTrueOrderByNameAsc();

    /** Progetti attivi a cui l'utente è assegnato, ordinati per nome. */
    @Query("""
            select up.project from UserProject up
            where up.user.id = :userId and up.project.active = true
            order by up.project.name
            """)
    List<Project> findActiveAssignedTo(@Param("userId") Long userId);
}
