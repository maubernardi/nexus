package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByVatCode(String vatCode);
}
