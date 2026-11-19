package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Company;
import it.nexus.domain.CompanyPlaceholders;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByVatCode(String vatCode);

    /**
     * Ricerca per ragione sociale (senza distinzione di maiuscole) o partita IVA, ordinata per nome; con il comune della
     * sede legale (US-1007) già caricato. Con {@code toComplete} solo le aziende con un dato da completare (US-508).
     */
    default List<Company> search(String search, boolean includeInactive, boolean toComplete) {
        return search(search, includeInactive, toComplete, CompanyPlaceholders.VAT_CODE,
                CompanyPlaceholders.NAME_PREFIX + "%");
    }

    @EntityGraph(attributePaths = {"legalMunicipality", "legalMunicipality.province"})
    @Query("""
            select c from Company c
            where (:includeInactive = true or c.active = true)
              and (:search is null
                   or lower(c.name) like lower(concat('%', cast(:search as string), '%'))
                   or c.vatCode like concat(cast(:search as string), '%'))
              and (:toComplete = false or c.vatCode = :missingVatCode or c.name like :missingNamePattern)
            order by lower(c.name), c.id
            """)
    List<Company> search(@Param("search") String search, @Param("includeInactive") boolean includeInactive,
            @Param("toComplete") boolean toComplete, @Param("missingVatCode") String missingVatCode,
            @Param("missingNamePattern") String missingNamePattern);
}
