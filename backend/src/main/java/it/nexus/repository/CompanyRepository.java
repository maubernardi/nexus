package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Ricerca per ragione sociale (senza distinzione di maiuscole) o partita IVA, ordinata per nome, una pagina alla
     * volta (US-513); con il comune della sede legale (US-1007), la categoria merceologica e la sede operativa (US-510)
     * già caricati; i referenti arrivano a lotti ({@code @BatchSize} su {@link Company#getContacts()}). Con
     * {@code toComplete} solo le aziende con un dato da completare (US-508); con {@code businessCategoryId} solo quelle
     * della categoria (US-509); con {@code minReliability} e {@code minProjectInterest} solo quelle con almeno quelle
     * stelle e quei pollici (US-514). La pagina non deve avere un ordinamento suo: vale quello della query.
     */
    default Page<Company> search(String search, boolean includeInactive, boolean toComplete, Long businessCategoryId,
            Integer minReliability, Integer minProjectInterest, Pageable page) {
        return search(search, includeInactive, toComplete, CompanyPlaceholders.VAT_CODE,
                CompanyPlaceholders.NAME_PREFIX + "%", businessCategoryId, minReliability, minProjectInterest, page);
    }

    @EntityGraph(attributePaths = {"legalMunicipality", "legalMunicipality.province", "businessCategory",
            "operatingMunicipality", "operatingMunicipality.province"})
    @Query(value = """
            select c from Company c
            where (:includeInactive = true or c.active = true)
              and (:search is null
                   or lower(c.name) like lower(concat('%', cast(:search as string), '%'))
                   or c.vatCode like concat(cast(:search as string), '%'))
              and (:toComplete = false or c.vatCode = :missingVatCode or c.name like :missingNamePattern)
              and (:businessCategoryId is null or c.businessCategory.id = :businessCategoryId)
              and (:minReliability is null or c.reliability >= :minReliability)
              and (:minProjectInterest is null or c.projectInterest >= :minProjectInterest)
            order by lower(c.name), c.id
            """, countQuery = """
            select count(c) from Company c
            where (:includeInactive = true or c.active = true)
              and (:search is null
                   or lower(c.name) like lower(concat('%', cast(:search as string), '%'))
                   or c.vatCode like concat(cast(:search as string), '%'))
              and (:toComplete = false or c.vatCode = :missingVatCode or c.name like :missingNamePattern)
              and (:businessCategoryId is null or c.businessCategory.id = :businessCategoryId)
              and (:minReliability is null or c.reliability >= :minReliability)
              and (:minProjectInterest is null or c.projectInterest >= :minProjectInterest)
            """)
    Page<Company> search(@Param("search") String search, @Param("includeInactive") boolean includeInactive,
            @Param("toComplete") boolean toComplete, @Param("missingVatCode") String missingVatCode,
            @Param("missingNamePattern") String missingNamePattern,
            @Param("businessCategoryId") Long businessCategoryId, @Param("minReliability") Integer minReliability,
            @Param("minProjectInterest") Integer minProjectInterest, Pageable page);
}
