package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.nexus.domain.AppUser;
import it.nexus.domain.UserProject;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, UserProject.Id> {

    List<UserProject> findByIdUserId(Long userId);

    /** Tutte le assegnazioni con il progetto, per l'elenco degli utenti in una sola query (US-104). */
    @Query("select up from UserProject up join fetch up.project")
    List<UserProject> findAllWithProject();

    /** Assegnazioni di un utente con il progetto (US-104). */
    @Query("select up from UserProject up join fetch up.project where up.user.id = :userId")
    List<UserProject> findByUserIdWithProject(@Param("userId") Long userId);

    /** Tutor attivi assegnati al progetto, per cognome e nome (US-111). */
    @Query("""
            select up.user from UserProject up
            where up.project.id = :projectId and up.user.active = true
              and up.user.role = it.nexus.domain.enumeration.Role.TUTOR
            order by up.user.lastName, up.user.firstName
            """)
    List<AppUser> findActiveTutorsOf(@Param("projectId") Long projectId);

    /** Progetti che l'utente coordina, con il progetto, per nome (US-112). */
    @Query("""
            select up from UserProject up join fetch up.project
            where up.user.id = :userId and up.coordinator = true
            order by lower(up.project.name), up.project.id
            """)
    List<UserProject> findCoordinatedBy(@Param("userId") Long userId);

    /** Vero se l'utente (per identificativo di Keycloak) coordina almeno un progetto (US-112). */
    boolean existsByUserExternalIdAndCoordinatorTrue(String externalId);
}
