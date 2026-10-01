package it.nexus.services.impl;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.SecurityUtils;
import it.nexus.config.security.UserNotEnabledException;
import it.nexus.domain.AppUser;
import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.services.CurrentAppUserService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CurrentAppUserServiceImpl implements CurrentAppUserService {

    private final AppUserRepository appUserRepository;

    @Override
    public AppUser getCurrentAppUser() {
        AuthenticatedUser principal = principal();
        return appUserRepository.findByExternalId(principal.id())
                .filter(AppUser::isActive)
                .orElseThrow(UserNotEnabledException::new);
    }

    @Override
    public boolean hasGlobalVisibility() {
        AuthenticatedUser principal = principal();
        return principal.roles().contains(Role.CALL_CENTER) || principal.roles().contains(Role.ADMIN);
    }

    private static AuthenticatedUser principal() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Nessun utente autenticato"));
    }
}
