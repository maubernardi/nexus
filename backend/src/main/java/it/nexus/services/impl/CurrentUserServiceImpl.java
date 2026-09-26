package it.nexus.services.impl;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

import it.nexus.config.security.SecurityUtils;
import it.nexus.domain.dto.CurrentUserDTO;
import it.nexus.mapper.CurrentUserMapper;
import it.nexus.services.CurrentUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final CurrentUserMapper mapper;

    @Override
    public CurrentUserDTO getCurrentUser() {
        return SecurityUtils.getCurrentUser()
                .map(mapper::toDto)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Nessun utente autenticato"));
    }
}
