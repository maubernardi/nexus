package it.nexus.web.errors;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato uniforme di tutte le risposte di errore delle API.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponseDTO(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        /** Codice stabile per i casi che il client gestisce in modo dedicato (es. {@code USER_NOT_ENABLED}). */
        String code,
        List<FieldErrorDTO> fieldErrors) {

    public record FieldErrorDTO(String field, String message) {
    }

    public static ApiErrorResponseDTO of(HttpStatus status, String message, String path) {
        return of(status, message, path, List.of());
    }

    public static ApiErrorResponseDTO of(HttpStatus status, String message, String path, List<FieldErrorDTO> fieldErrors) {
        return new ApiErrorResponseDTO(Instant.now(), status.value(), status.getReasonPhrase(), message, path, null,
                fieldErrors);
    }

    public static ApiErrorResponseDTO withCode(HttpStatus status, String code, String message, String path) {
        return new ApiErrorResponseDTO(Instant.now(), status.value(), status.getReasonPhrase(), message, path, code,
                List.of());
    }
}
