package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByExternalId(String externalId);

    Optional<AppUser> findByUsername(String username);
}
