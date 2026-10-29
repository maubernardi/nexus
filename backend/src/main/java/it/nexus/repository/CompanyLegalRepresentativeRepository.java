package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.CompanyLegalRepresentative;

@Repository
public interface CompanyLegalRepresentativeRepository extends JpaRepository<CompanyLegalRepresentative, Long> {

    Optional<CompanyLegalRepresentative> findByCompanyId(Long companyId);
}
