package it.nexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.UserProject;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, UserProject.Id> {

    List<UserProject> findByIdUserId(Long userId);
}
