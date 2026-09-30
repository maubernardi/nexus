package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Candidate;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @EntityGraph(attributePaths = "residenceZone")
    List<Candidate> findByOwnerTutorIdOrderByLastNameAscFirstNameAsc(Long ownerTutorId);
}
