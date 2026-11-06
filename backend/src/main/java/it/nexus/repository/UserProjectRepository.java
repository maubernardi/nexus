package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
