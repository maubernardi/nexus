package it.nexus.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import it.nexus.services.RegisteredUserService;
import lombok.RequiredArgsConstructor;

/**
 * Filter chain OAuth2 resource server (JWT Keycloak), stateless, deny by default.
 */
@Configuration
@Profile("!security-mock")
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakJwtConverter jwtConverter;
    private final SecurityErrorHandlers errorHandlers;
    private final RegisteredUserService registeredUserService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(RequestsAuthorizer.PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(jwtConverter))
                        .authenticationEntryPoint(errorHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(errorHandlers.accessDeniedHandler()))
                .addFilterBefore(new RegisteredUserFilter(registeredUserService), AuthorizationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(errorHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(errorHandlers.accessDeniedHandler()));
        return http.build();
    }
}
