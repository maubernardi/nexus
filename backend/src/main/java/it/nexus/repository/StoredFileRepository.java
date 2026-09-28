package it.nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.StoredFile;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
}
