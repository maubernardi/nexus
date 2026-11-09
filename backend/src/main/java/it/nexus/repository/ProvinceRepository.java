package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

    /** Versioni attuali (ancora valide), per il confronto con il file ISTAT (US-114). */
    List<Province> findByValidToIsNull();

    long countByValidToIsNull();
}
