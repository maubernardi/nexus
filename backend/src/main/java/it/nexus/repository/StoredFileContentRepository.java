package it.nexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.StoredFileContent;

@Repository
public interface StoredFileContentRepository extends JpaRepository<StoredFileContent, Long> {
}
