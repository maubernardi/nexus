package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.BoardPost;

@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    Optional<BoardPost> findByNumber(Long number);
}
