package it.nexus.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByExternalId(String externalId);

    /** Utenti dei tentativi di accesso falliti, per mostrarne il nome (US-113). */
    List<AppUser> findByExternalIdIn(Collection<String> externalIds);

    Optional<AppUser> findByUsername(String username);

    List<AppUser> findByUsernameIn(Collection<String> usernames);

    /** Elenco per l'ADMIN (US-103): prima gli attivi, poi per cognome e nome. */
    List<AppUser> findAllByOrderByActiveDescLastNameAscFirstNameAsc();

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    /** Indirizzo già usato da un altro utente (unicità senza distinzione tra maiuscole e minuscole). */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
