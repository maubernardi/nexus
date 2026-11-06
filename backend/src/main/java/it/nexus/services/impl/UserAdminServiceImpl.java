package it.nexus.services.impl;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.ApplicationProperties;
import it.nexus.domain.AppUser;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.UserCreateDTO;
import it.nexus.domain.dto.UserCreatedDTO;
import it.nexus.domain.dto.UserDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.services.AuditService;
import it.nexus.services.UserAdminService;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.identity.NewIdentityUser;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import it.nexus.web.errors.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    /** Validità del collegamento per impostare la password (decisione del PO, 06/11). */
    static final Duration INVITATION_VALIDITY = Duration.ofDays(3);

    private final AppUserRepository appUserRepository;
    private final IdentityProviderUsers identityProviderUsers;
    private final AuditService auditService;
    private final ApplicationProperties applicationProperties;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> list() {
        return appUserRepository.findAllByOrderByActiveDescLastNameAscFirstNameAsc().stream()
                .map(UserAdminServiceImpl::toDto).toList();
    }

    @Override
    public UserCreatedDTO create(UserCreateDTO dto) {
        // come Keycloak, che salva username ed email in minuscolo: le due copie restano identiche
        String username = dto.username().strip().toLowerCase(Locale.ROOT);
        String email = dto.email().strip().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new FieldValidationException("username", "Username già usato da un altro utente");
        }
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new FieldValidationException("email", "Indirizzo già usato da un altro utente");
        }
        String firstName = dto.firstName().strip();
        String lastName = dto.lastName().strip();
        String phone = dto.phone() == null || dto.phone().isBlank() ? null : dto.phone().strip();

        // prima Keycloak, che dà l'identificativo; se NEXUS poi non salva, l'account si cancella
        String externalId;
        try {
            externalId = identityProviderUsers.createUser(
                    new NewIdentityUser(username, firstName, lastName, email, dto.role()));
        } catch (IdentityProviderException e) {
            if (e.isUsernameTaken()) {
                throw new FieldValidationException("username", "Username già usato da un account di accesso");
            }
            if (e.isEmailTaken()) {
                throw new FieldValidationException("email", "Indirizzo già usato da un account di accesso");
            }
            throw new ServiceUnavailableException(
                    "Non è stato possibile creare l'account di accesso: l'utente non è stato creato, riprova più tardi");
        }
        AppUser saved;
        try {
            saved = appUserRepository.saveAndFlush(
                    new AppUser(externalId, username, firstName, lastName, email, phone, dto.role()));
        } catch (RuntimeException e) {
            undo(externalId);
            throw e;
        }
        auditService.record(AuditEntityType.APP_USER, saved.getId(), "CREATE", AuditChanges.of()
                .value("username", null, username)
                .value("role", null, dto.role().name())
                .redacted("firstName")
                .redacted("lastName")
                .redacted("email")
                .merge(phone == null ? null : AuditChanges.of().redacted("phone")), null);
        log.info("Utente {} creato con il ruolo {}", saved, dto.role());
        return new UserCreatedDTO(toDto(saved), invite(saved));
    }

    @Override
    public void resendInvitation(String id) {
        AppUser user = Optional.ofNullable(TsidMapper.toInternal(id)).flatMap(appUserRepository::findById)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        if (!user.isActive()) {
            throw new BadRequestException("L'utente è disattivato: riattivalo prima di invitarlo");
        }
        if (!invite(user)) {
            throw new ServiceUnavailableException(
                    "L'email per impostare la password non è partita: controlla la configurazione email di Keycloak e riprova");
        }
    }

    /** Email per impostare la password; un errore non annulla la creazione (si reinvia dall'elenco). */
    private boolean invite(AppUser user) {
        try {
            identityProviderUsers.sendPasswordSetupEmail(user.getExternalId(), INVITATION_VALIDITY,
                    applicationProperties.getMail().getAppUrl() + "/");
        } catch (IdentityProviderException e) {
            log.warn("Email per la password di {} non inviata: {}", user, e.getMessage());
            return false;
        }
        auditService.record(AuditEntityType.APP_USER, user.getId(), "INVITATION_SENT", AuditChanges.none(), null);
        return true;
    }

    private void undo(String externalId) {
        try {
            identityProviderUsers.deleteUser(externalId);
        } catch (IdentityProviderException e) {
            log.error("Account Keycloak {} creato ma utente NEXUS non salvato: va rimosso dalla console", externalId);
        }
    }

    private static UserDTO toDto(AppUser user) {
        return new UserDTO(TsidMapper.toExternal(user.getId()), user.getUsername(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getPhone(), user.getRole(), user.isActive());
    }
}
