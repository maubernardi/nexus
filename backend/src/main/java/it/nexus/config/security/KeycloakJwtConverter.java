package it.nexus.config.security;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import it.nexus.domain.enumeration.Role;

/**
 * Converte un JWT Keycloak in {@link UserAuthenticationToken}: i realm role {@code TUTOR|CALL_CENTER|ADMIN}
 * diventano authority {@code ROLE_*}; gli altri ruoli (es. default-roles-nexus) vengono ignorati.
 */
@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AuthenticatedUser user = new AuthenticatedUser(
                jwt.getSubject(),
                Optional.ofNullable(jwt.getClaimAsString("preferred_username")).orElse(jwt.getSubject()),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"),
                jwt.getClaimAsString("email"),
                extractRoles(jwt));
        return new UserAuthenticationToken(user, jwt);
    }

    private Set<Role> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return Set.of();
        }
        return roles.stream()
                .map(String::valueOf)
                .map(Role::fromName)
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableSet());
    }
}
