package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Project;
import it.nexus.domain.ProjectVariable;

@Repository
public interface ProjectVariableRepository extends JpaRepository<ProjectVariable, Long> {

    /** Le variabili del progetto, per nome (US-1013). */
    List<ProjectVariable> findByProjectOrderByNameAsc(Project project);

    /** Tutte le variabili con il loro progetto, per l'elenco dei segnaposto: per progetto, poi per nome. */
    @Query("""
            select v from ProjectVariable v join fetch v.project p
            order by p.name, v.name
            """)
    List<ProjectVariable> findAllWithProject();
}
