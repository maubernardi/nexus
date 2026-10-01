package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByVatCode(String vatCode);

    /** Ricerca per ragione sociale (senza distinzione di maiuscole) o partita IVA, ordinata per nome. */
    @Query("""
            select c from Company c
            where (:includeInactive = true or c.active = true)
              and (:search is null
                   or lower(c.name) like lower(concat('%', cast(:search as string), '%'))
                   or c.vatCode like concat(cast(:search as string), '%'))
            order by lower(c.name), c.id
            """)
    List<Company> search(@Param("search") String search, @Param("includeInactive") boolean includeInactive);
}
