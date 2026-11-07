package it.nexus.services;

import it.nexus.domain.dto.AccessLogPageDTO;

/** Registro degli accessi, letto dagli eventi di Keycloak che li conserva 12 mesi (US-113). */
public interface AccessLogService {

    /** Righe per pagina. */
    int PAGE_SIZE = 20;

    /**
     * Accessi riusciti e falliti di un utente, per l'ADMIN.
     *
     * @throws it.nexus.web.errors.NotFoundException utente inesistente
     * @throws it.nexus.web.errors.ServiceUnavailableException Keycloak non risponde
     */
    AccessLogPageDTO forUser(String userId, int page);

    /** I propri accessi, per ogni utente. */
    AccessLogPageDTO mine(int page);

    /** Tentativi falliti di tutti, anche con uno username inesistente, per l'ADMIN. */
    AccessLogPageDTO failed(int page);
}
