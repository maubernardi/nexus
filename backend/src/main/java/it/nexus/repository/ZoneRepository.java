package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Zone;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    Optional<Zone> findByCode(String code);

    List<Zone> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-106): prima le attive, poi per nome. */
    List<Zone> findAllByOrderByActiveDescNameAsc();

    /** Zona attiva con lo stesso nome, senza distinguere maiuscole e minuscole ({@code uq_zone_active_name}). */
    Optional<Zone> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
