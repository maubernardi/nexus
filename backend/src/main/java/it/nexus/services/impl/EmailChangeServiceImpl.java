package it.nexus.services.impl;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.EmailChangeRequest;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.EmailStatusDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.EmailChangeRequestRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.EmailChangeService;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.mail.EmailChangeMailer;
import it.nexus.services.mail.EmailChangedEvent;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailChangeServiceImpl implements EmailChangeService {

    /** Validità del codice (decisione del PO, 05/11). */
    static final Duration CODE_VALIDITY = Duration.ofMinutes(30);
    /** Attesa minima tra due invii del codice. */
    static final Duration RESEND_DELAY = Duration.ofSeconds(60);

    private static final String FIELD = "newEmail";
    private static final String UNAVAILABLE =
            "Non è stato possibile inviare il codice: il cambio dell'indirizzo non è disponibile al momento, riprova più tardi";

    private final CurrentAppUserService currentAppUserService;
    private final AppUserRepository appUserRepository;
    private final EmailChangeRequestRepository requestRepository;
    private final IdentityProviderUsers identityProviderUsers;
    private final EmailChangeMailer mailer;
    private final AuditService auditService;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final PasswordEncoder codeEncoder = new BCryptPasswordEncoder();


    @Override
    @Transactional(readOnly = true)
    public EmailStatusDTO status() {
        AppUser user = currentAppUserService.getCurrentAppUser();
        return toStatus(user, requestRepository.findByUserId(user.getId()));
    }

    @Override
    public EmailStatusDTO request(String newEmail) {
        AppUser user = currentAppUserService.getCurrentAppUser();
        String email = normalize(newEmail);
        if (email.equalsIgnoreCase(user.getEmail())) {
            throw new FieldValidationException(FIELD, "È già il tuo indirizzo attuale");
        }
        if (appUserRepository.existsByEmailIgnoreCaseAndIdNot(email, user.getId())) {
            throw new FieldValidationException(FIELD, "Indirizzo già usato da un altro utente");
        }
        Instant now = clock.instant();
        Optional<EmailChangeRequest> existing = requestRepository.findByUserId(user.getId());
        existing.map(r -> resendAvailableAt(r)).filter(now::isBefore).ifPresent(at -> {
            throw new BadRequestException("Hai appena chiesto un codice: puoi chiederne un altro tra %d secondi",
                    Math.max(1, Duration.between(now, at).toSeconds()));
        });

        String code = "%06d".formatted(random.nextInt(1_000_000));
        Instant expiresAt = now.plus(CODE_VALIDITY);
        String hash = codeEncoder.encode(code);
        EmailChangeRequest request = existing.orElse(null);
        if (request == null) {
            request = new EmailChangeRequest(user, email, hash, now, expiresAt);
        } else {
            request.renew(email, hash, now, expiresAt);
        }
        request = requestRepository.saveAndFlush(request);
        // invio sincrono: se l'email non parte la transazione si annulla e la richiesta non resta
        if (!mailer.sendCode(email, code, expiresAt)) {
            throw new ServiceUnavailableException(UNAVAILABLE);
        }
        return toStatus(user, Optional.of(request));
    }

    // l'errore sul codice deve salvare il tentativo consumato
    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public EmailStatusDTO confirm(String code) {
        AppUser user = currentAppUserService.getCurrentAppUser();
        EmailChangeRequest request = requestRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Nessun cambio di indirizzo in corso: chiedi un nuovo codice"));
        if (request.isExpired(clock.instant())) {
            throw new BadRequestException("Il codice è scaduto: chiedine uno nuovo");
        }
        if (request.remainingAttempts() <= 0) {
            throw new BadRequestException("Troppi tentativi sbagliati: chiedi un nuovo codice");
        }
        if (!codeEncoder.matches(code.strip(), request.getCodeHash())) {
            request.recordFailedAttempt();
            int remaining = request.remainingAttempts();
            throw new BadRequestException(remaining > 0
                    ? "Codice non valido: %d %s".formatted(remaining, remaining == 1 ? "tentativo rimasto" : "tentativi rimasti")
                    : "Codice non valido. Troppi tentativi sbagliati: chiedi un nuovo codice");
        }
        String newEmail = request.getNewEmail();
        if (appUserRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, user.getId())) {
            throw new FieldValidationException(FIELD, "Indirizzo già usato da un altro utente");
        }

        // prima Keycloak: se rifiuta, in NEXUS non cambia nulla
        try {
            identityProviderUsers.updateEmail(user.getExternalId(), newEmail);
        } catch (IdentityProviderException e) {
            if (e.isEmailTaken()) {
                throw new FieldValidationException(FIELD, "Indirizzo già usato da un altro account");
            }
            throw new ServiceUnavailableException(
                    "Non è stato possibile aggiornare l'account di accesso: l'indirizzo non è cambiato, riprova più tardi");
        }
        String previousEmail = user.getEmail();
        user.setEmail(newEmail);
        requestRepository.delete(request);
        appUserRepository.saveAndFlush(user);
        auditService.record(AuditEntityType.APP_USER, user.getId(), "EMAIL_CHANGED",
                AuditChanges.of().redacted("email"), null);
        events.publishEvent(new EmailChangedEvent(user.getId(), previousEmail));
        log.info("Email di {} cambiata dopo la conferma con il codice", user);
        return toStatus(user, Optional.empty());
    }

    @Override
    public EmailStatusDTO cancel() {
        AppUser user = currentAppUserService.getCurrentAppUser();
        requestRepository.findByUserId(user.getId()).ifPresent(requestRepository::delete);
        return toStatus(user, Optional.empty());
    }

    private static String normalize(String email) {
        // come Keycloak, che salva le email in minuscolo: le due copie restano identiche
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static Instant resendAvailableAt(EmailChangeRequest request) {
        return request.getSentAt().plus(RESEND_DELAY);
    }

    private EmailStatusDTO toStatus(AppUser user, Optional<EmailChangeRequest> request) {
        Instant now = clock.instant();
        EmailStatusDTO.Pending pending = request
                .filter(r -> !r.isExpired(now))
                .map(r -> new EmailStatusDTO.Pending(r.getNewEmail(), r.getExpiresAt(), resendAvailableAt(r),
                        Math.max(0, r.remainingAttempts())))
                .orElse(null);
        return new EmailStatusDTO(user.getEmail(), pending);
    }
}
