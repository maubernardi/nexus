package it.nexus.config.security;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Autentica la richiesta a partire dall'header {@value #HEADER}, cercando l'utente tra quelli mock configurati.
 * Header assente o utente sconosciuto: la richiesta resta anonima (401 sugli endpoint protetti).
 */
public class MockHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-USER-ID";

    private final Map<String, AuthenticatedUser> users;

    public MockHeaderAuthenticationFilter(MockSecurityProperties properties) {
        this.users = properties.users().stream()
                .map(MockSecurityProperties.MockUser::toAuthenticatedUser)
                .collect(Collectors.toUnmodifiableMap(AuthenticatedUser::username, Function.identity()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String userId = request.getHeader(HEADER);
        AuthenticatedUser user = userId == null ? null : users.get(userId);
        if (user != null) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UserAuthenticationToken(user, null));
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }
}
