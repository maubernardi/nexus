package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.DocumentTemplate;
import it.nexus.domain.enumeration.InternshipDocumentKind;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

    /** Elenco per l'ADMIN, con le condizioni e il file in una query. */
    @EntityGraph(attributePaths = {"project", "internshipType", "careAgency", "file"})
    List<DocumentTemplate> findAllByOrderByKindAscNameAsc();

    /** Modelli attivi di un documento per progetto e tipo: l'ente si filtra nel servizio (US-1010, D4). */
    @EntityGraph(attributePaths = {"careAgency", "file"})
    List<DocumentTemplate> findByKindAndProjectIdAndInternshipTypeIdAndActiveTrueOrderByNameAsc(
            InternshipDocumentKind kind, Long projectId, Long internshipTypeId);
}
