package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.CompanyAtecoCode;

@Repository
public interface CompanyAtecoCodeRepository extends JpaRepository<CompanyAtecoCode, Long> {

    List<CompanyAtecoCode> findByCompanyIdOrderByCodeAsc(Long companyId);

    Optional<CompanyAtecoCode> findByCompanyIdAndCode(Long companyId, String code);
}
