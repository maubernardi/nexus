package it.nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.Candidate;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
}
