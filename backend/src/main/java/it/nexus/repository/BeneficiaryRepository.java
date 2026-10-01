package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Beneficiary;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    @EntityGraph(attributePaths = "residenceZone")
    List<Beneficiary> findByOwnerTutorIdOrderByLastNameAscFirstNameAsc(Long ownerTutorId);
}
