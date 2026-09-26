package it.nexus.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sicurezza fittizia per sviluppo locale e test: autenticazione tramite header {@code X-USER-ID}.
 * NON attivare mai in ambienti condivisi.
 */
@Slf4j
@Configuration
@Profile("security-mock")
@RequiredArgsConstructor
public class MockSecurityConfig {

    private final MockSecurityProperties properties;
    private final SecurityErrorHandlers errorHandlers;

    @Bean
    SecurityFilterChain mockSecurityFilterChain(HttpSecurity http) throws Exception {
        log.warn("MOCK Security abilitata (header {}): NON USARE IN PRODUZIONE", MockHeaderAuthenticationFilter.HEADER);
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(RequestsAuthorizer.PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new MockHeaderAuthenticationFilter(properties), AnonymousAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(errorHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(errorHandlers.accessDeniedHandler()));
        return http.build();
    }
}
