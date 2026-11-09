package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.MunicipalityImport;

@Repository
public interface MunicipalityImportRepository extends JpaRepository<MunicipalityImport, Long> {

    /** L'ultimo caricamento: un nuovo file deve valere da una data successiva. */
    Optional<MunicipalityImport> findFirstByOrderByValidFromDesc();
}
