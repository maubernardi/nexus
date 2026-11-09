package it.nexus.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Municipality;

@Repository
public interface MunicipalityRepository extends JpaRepository<Municipality, Long> {

    /** Versioni attuali con la provincia, per il confronto con il file ISTAT (US-114). */
    @EntityGraph(attributePaths = "province")
    List<Municipality> findByValidToIsNull();

    long countByValidToIsNull();

    /**
     * Ricerca per nome (contiene, senza distinguere maiuscole e minuscole): prima i nomi che iniziano con il testo, poi
     * per nome; con {@code history} anche le versioni non più valide. Il testo arriva già in minuscolo, con {@code %},
     * {@code _} e {@code !} preceduti da {@code !}.
     */
    @EntityGraph(attributePaths = "province")
    @Query("""
            SELECT m FROM Municipality m
            WHERE lower(m.name) LIKE concat('%', :q, '%') ESCAPE '!' AND (:history = true OR m.validTo IS NULL)
            ORDER BY CASE WHEN lower(m.name) LIKE concat(:q, '%') ESCAPE '!' THEN 0 ELSE 1 END, m.name, m.validFrom DESC
            """)
    List<Municipality> search(@Param("q") String lowerText, @Param("history") boolean history, Pageable page);
}
