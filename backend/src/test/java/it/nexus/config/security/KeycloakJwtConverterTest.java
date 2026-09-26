package it.nexus.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import it.nexus.domain.enumeration.Role;

class KeycloakJwtConverterTest {

    private final KeycloakJwtConverter converter = new KeycloakJwtConverter();

    @Test
    void convert_mapsKnownRealmRolesAndIgnoresOthers() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("sub-123")
                .claim("preferred_username", "operatore.cc")
                .claim("given_name", "Operatore")
                .claim("family_name", "Call Center")
                .claim("email", "operatore.cc@nexus.local")
                .claim("realm_access", Map.of("roles", List.of("CALL_CENTER", "default-roles-nexus", "offline_access")))
                .build();

        UserAuthenticationToken token = (UserAuthenticationToken) converter.convert(jwt);

        assertThat(token.getPrincipal().id()).isEqualTo("sub-123");
        assertThat(token.getPrincipal().username()).isEqualTo("operatore.cc");
        assertThat(token.getPrincipal().roles()).containsExactly(Role.CALL_CENTER);
        assertThat(token.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_CALL_CENTER");
    }

    @Test
    void convert_returnsNoRoles_whenRealmAccessMissing() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject("sub-1").build();

        UserAuthenticationToken token = (UserAuthenticationToken) converter.convert(jwt);

        assertThat(token.getPrincipal().username()).isEqualTo("sub-1");
        assertThat(token.getAuthorities()).isEmpty();
    }
}
