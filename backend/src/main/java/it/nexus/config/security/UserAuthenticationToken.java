package it.nexus.config.security;

import java.io.Serial;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import it.nexus.domain.enumeration.Role;

/**
 * Authentication con {@link AuthenticatedUser} come principal e authority {@code ROLE_*} derivate dai ruoli.
 */
public class UserAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 1L;

    private final AuthenticatedUser user;
    private final transient Object credentials;

    public UserAuthenticationToken(AuthenticatedUser user, Object credentials) {
        super(user.roles().stream().map(Role::authority).map(SimpleGrantedAuthority::new).toList());
        this.user = user;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public AuthenticatedUser getPrincipal() {
        return user;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public String getName() {
        return user.username();
    }
}
