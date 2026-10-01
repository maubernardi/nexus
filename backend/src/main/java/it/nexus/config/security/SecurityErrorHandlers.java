package it.nexus.config.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import it.nexus.web.errors.ApiErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

/**
 * Serializza 401/403 generati dalla filter chain nel formato errori uniforme.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorHandlers {

    private final JsonMapper jsonMapper;

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> write(request, response,
                ApiErrorResponseDTO.of(HttpStatus.UNAUTHORIZED, "Autenticazione richiesta", request.getRequestURI()));
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> write(request, response, ex instanceof UserNotEnabledException
                ? ApiErrorResponseDTO.withCode(HttpStatus.FORBIDDEN, UserNotEnabledException.CODE,
                        UserNotEnabledException.MESSAGE, request.getRequestURI())
                : ApiErrorResponseDTO.of(HttpStatus.FORBIDDEN, "Accesso negato", request.getRequestURI()));
    }

    private void write(HttpServletRequest request, HttpServletResponse response, ApiErrorResponseDTO body)
            throws IOException {
        response.setStatus(body.status());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getOutputStream(), body);
    }
}
