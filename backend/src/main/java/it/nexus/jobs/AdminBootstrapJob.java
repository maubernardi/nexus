package it.nexus.jobs;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.nexus.config.ApplicationProperties;
import it.nexus.domain.dto.UserCreateDTO;
import it.nexus.domain.dto.UserCreatedDTO;
import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.services.UserAdminService;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.ServiceUnavailableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Primo ADMIN della produzione (US-1306). Dopo il passaggio in produzione in NEXUS non c'è nessun utente: se i dati
 * dell'ADMIN sono nel {@code .env} (NEXUS_ADMIN_*), questo controllo lo crea come farebbe un ADMIN da «Utenti»
 * (US-103): account in Keycloak, utente in NEXUS, audit e invito per email. All'avvio Keycloak può non essere ancora
 * pronto: riprova ogni minuto finché non riesce, poi si ferma. Con un ADMIN già presente, o senza i dati, non fa
 * nulla.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapJob {

    private final ApplicationProperties properties;
    private final AppUserRepository appUserRepository;
    private final UserAdminService userAdminService;
    private final Validator validator;

    private final AtomicBoolean done = new AtomicBoolean();

    @Scheduled(initialDelayString = "${nexus.bootstrap.admin.initial-delay:PT30S}",
            fixedDelayString = "${nexus.bootstrap.admin.retry-delay:PT1M}")
    public void run() {
        if (!done.get()) {
            done.set(createIfMissing());
        }
    }

    /**
     * Crea l'ADMIN iniziale se manca.
     *
     * @return {@code true} se non c'è più niente da fare: ADMIN presente o creato, oppure dati assenti o sbagliati;
     *         {@code false} se va ritentato, perché Keycloak non ha risposto
     */
    boolean createIfMissing() {
        ApplicationProperties.Admin admin = properties.getBootstrap().getAdmin();
        if (appUserRepository.existsByRole(Role.ADMIN)) {
            return true;
        }
        if (!admin.isComplete()) {
            log.warn("In NEXUS non c'è nessun ADMIN e i dati dell'ADMIN iniziale (NEXUS_ADMIN_* nel .env) non sono "
                    + "completi: nessuno può gestire gli utenti");
            return true;
        }
        UserCreateDTO dto = new UserCreateDTO(admin.getUsername(), admin.getFirstName(), admin.getLastName(),
                admin.getEmail(), null, Role.ADMIN);
        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            log.error("ADMIN iniziale non creato, dati del .env non validi: {}", violations.stream()
                    .map(v -> v.getPropertyPath() + " (" + v.getMessage() + ")").sorted()
                    .collect(Collectors.joining(", ")));
            return true;
        }
        try {
            UserCreatedDTO created = userAdminService.create(dto);
            if (created.invitationSent()) {
                log.info("ADMIN iniziale creato: l'email per impostare la password è partita");
            } else {
                log.warn("ADMIN iniziale creato, ma l'email per impostare la password non è partita: usa «Password "
                        + "dimenticata?» nella pagina di accesso");
            }
            return true;
        } catch (FieldValidationException e) {
            log.error("ADMIN iniziale non creato: {}", e.getMessage());
            return true;
        } catch (ServiceUnavailableException e) {
            log.warn("ADMIN iniziale non ancora creato, Keycloak non risponde: riprovo tra un minuto");
            return false;
        }
    }
}
