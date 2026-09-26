package it.nexus.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.UserAuthenticationToken;
import it.nexus.domain.dto.CurrentUserDTO;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.CurrentUserMapper;
import it.nexus.services.impl.CurrentUserServiceImpl;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    CurrentUserMapper mapper;

    @InjectMocks
    CurrentUserServiceImpl service;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_returnsDto_whenAuthenticated() {
        AuthenticatedUser user = new AuthenticatedUser("id-1", "tutor1", "Tutor", "Uno", "t@x.it", Set.of(Role.TUTOR));
        SecurityContextHolder.getContext().setAuthentication(new UserAuthenticationToken(user, null));
        CurrentUserDTO dto = new CurrentUserDTO("id-1", "tutor1", "Tutor", "Uno", "t@x.it", Set.of(Role.TUTOR));
        when(mapper.toDto(user)).thenReturn(dto);

        assertThat(service.getCurrentUser()).isEqualTo(dto);
    }

    @Test
    void getCurrentUser_throws_whenAnonymous() {
        assertThatThrownBy(() -> service.getCurrentUser())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}
