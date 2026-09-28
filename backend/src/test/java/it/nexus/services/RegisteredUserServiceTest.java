package it.nexus.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.UserNotEnabledException;
import it.nexus.domain.AppUser;
import it.nexus.domain.TestEntities;
import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.services.impl.RegisteredUserServiceImpl;

@ExtendWith(MockitoExtension.class)
class RegisteredUserServiceTest {

    @Mock
    AppUserRepository appUserRepository;

    @InjectMocks
    RegisteredUserServiceImpl service;

    private static AuthenticatedUser principal(Set<Role> roles) {
        return new AuthenticatedUser("sub-1", "tutor1", "T", "U", "t@x.it", roles);
    }

    @Test
    void verify_throws_whenUserNotRegistered() {
        when(appUserRepository.findByExternalId("sub-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyAndSyncRole(principal(Set.of(Role.TUTOR))))
                .isInstanceOf(UserNotEnabledException.class);
    }

    @Test
    void verify_throws_whenUserDeactivated() {
        AppUser user = TestEntities.user("tutor1", Role.TUTOR, "sub-1");
        user.setActive(false);
        when(appUserRepository.findByExternalId("sub-1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.verifyAndSyncRole(principal(Set.of(Role.TUTOR))))
                .isInstanceOf(UserNotEnabledException.class);
    }

    @Test
    void verify_keepsRole_whenAligned() {
        AppUser user = TestEntities.user("tutor1", Role.TUTOR, "sub-1");
        when(appUserRepository.findByExternalId("sub-1")).thenReturn(Optional.of(user));

        service.verifyAndSyncRole(principal(Set.of(Role.TUTOR)));

        assertThat(user.getRole()).isEqualTo(Role.TUTOR);
    }

    @Test
    void verify_realignsToHighestTokenRole() {
        AppUser user = TestEntities.user("tutor1", Role.TUTOR, "sub-1");
        when(appUserRepository.findByExternalId("sub-1")).thenReturn(Optional.of(user));

        service.verifyAndSyncRole(principal(Set.of(Role.TUTOR, Role.ADMIN)));

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void verify_keepsRole_whenTokenHasNoNexusRole() {
        AppUser user = TestEntities.user("tutor1", Role.CALL_CENTER, "sub-1");
        when(appUserRepository.findByExternalId("sub-1")).thenReturn(Optional.of(user));

        service.verifyAndSyncRole(principal(Set.of()));

        assertThat(user.getRole()).isEqualTo(Role.CALL_CENTER);
    }
}
