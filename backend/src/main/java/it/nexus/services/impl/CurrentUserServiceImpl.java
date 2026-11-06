package it.nexus.services.impl;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

import it.nexus.config.security.SecurityUtils;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.dto.CurrentUserDTO;
import it.nexus.mapper.CurrentUserMapper;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.CurrentUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final CurrentUserMapper mapper;
    private final UserProjectRepository userProjectRepository;

    @Override
    public CurrentUserDTO getCurrentUser() {
        return SecurityUtils.getCurrentUser()
                .map(user -> mapper.toDto(user).withCoordinator(user.roles().contains(Role.TUTOR)
                        && userProjectRepository.existsByUserExternalIdAndCoordinatorTrue(user.id())))
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Nessun utente autenticato"));
    }
}
