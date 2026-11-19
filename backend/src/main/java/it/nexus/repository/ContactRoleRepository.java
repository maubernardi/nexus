package it.nexus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.ContactRole;

@Repository
public interface ContactRoleRepository extends JpaRepository<ContactRole, Long> {

    List<ContactRole> findByActiveTrueOrderByNameAsc();

    /** Catalogo per l'ADMIN (US-510): prima le voci attive, poi per nome. */
    List<ContactRole> findAllByOrderByActiveDescNameAsc();

    /** Voce attiva con lo stesso nome ({@code uq_contact_role_active_name}). */
    Optional<ContactRole> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByCode(String code);
}
