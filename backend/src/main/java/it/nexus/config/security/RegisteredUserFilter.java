package it.nexus.config.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import it.nexus.services.RegisteredUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Dopo l'autenticazione (JWT o mock) ammette solo gli utenti censiti e attivi in NEXUS e riallinea la copia del
 * ruolo. Va inserito nella catena di sicurezza prima dell'autorizzazione, dopo l'ExceptionTranslationFilter: la
 * {@link UserNotEnabledException} diventa così un 403 nel formato errori uniforme. Le richieste anonime (endpoint
 * pubblici) non sono toccate. Non è un bean, per non essere registrato anche fuori dalla catena di sicurezza.
 */
@RequiredArgsConstructor
public class RegisteredUserFilter extends OncePerRequestFilter {

    private final RegisteredUserService registeredUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            registeredUserService.verifyAndSyncRole(user);
        }
        chain.doFilter(request, response);
    }
}
