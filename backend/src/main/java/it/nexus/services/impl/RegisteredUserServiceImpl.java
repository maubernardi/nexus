package it.nexus.services.impl;

import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.UserNotEnabledException;
import it.nexus.domain.AppUser;
import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.services.RegisteredUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisteredUserServiceImpl implements RegisteredUserService {

    private final AppUserRepository appUserRepository;

    @Override
    public void verifyAndSyncRole(AuthenticatedUser user) {
        AppUser appUser = appUserRepository.findByExternalId(user.id())
                .filter(AppUser::isActive)
                .orElseThrow(UserNotEnabledException::new);

        // un token emesso prima dell'ultima modifica in NEXUS (es. il cambio di ruolo dell'ADMIN, US-105) ha il ruolo
        // vecchio: vale fino alla scadenza ma non riporta indietro NEXUS
        boolean staleToken = user.issuedAt() != null && appUser.getUpdatedAt() != null
                && user.issuedAt().isBefore(appUser.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS));
        Role.highest(user.roles())
                .filter(tokenRole -> !staleToken && tokenRole != appUser.getRole())
                .ifPresent(tokenRole -> {
                    // toString() dell'entità riporta solo tipo e id: nessun dato personale nei log
                    log.info("Ruolo di {} riallineato all'identity provider: {} -> {}",
                            appUser, appUser.getRole(), tokenRole);
                    appUser.setRole(tokenRole);
                });
    }
}
