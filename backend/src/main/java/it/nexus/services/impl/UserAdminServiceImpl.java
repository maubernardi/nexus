package it.nexus.services.impl;

import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.ApplicationProperties;
import it.nexus.domain.AppUser;
import it.nexus.domain.Project;
import it.nexus.domain.UserProject;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.UserCreateDTO;
import it.nexus.domain.dto.UserCreatedDTO;
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.domain.dto.UserDTO;
import it.nexus.domain.dto.UserProjectsDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.UserProjectRepository;
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

    private static final Comparator<Project> BY_NAME = Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER);

    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final IdentityProviderUsers identityProviderUsers;
    private final AuditService auditService;
    private final ApplicationProperties applicationProperties;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> list() {
        Map<Long, List<Project>> projectsByUser = userProjectRepository.findAllWithProject().stream()
                .collect(Collectors.groupingBy(up -> up.getId().getUserId(),
                        Collectors.mapping(UserProject::getProject, Collectors.toList())));
        return appUserRepository.findAllByOrderByActiveDescLastNameAscFirstNameAsc().stream()
                .map(u -> toDto(u, projectsByUser.getOrDefault(u.getId(), List.of()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO get(String id) {
        AppUser user = findUser(id);
        return toDto(user, assignedProjects(user));
    }

    @Override
    public UserDTO updateProjects(String id, UserProjectsDTO dto) {
        AppUser user = findUser(id);
        if (user.getRole() != Role.TUTOR) {
            throw new BadRequestException(
                    "Solo i Tutor hanno progetti assegnati: Call Center e ADMIN vedono già tutti i progetti");
        }
        Set<Long> requested = new LinkedHashSet<>();
        for (String projectId : dto.projectIds()) {
            Long internal = TsidMapper.toInternal(projectId);
            if (internal == null) {
                throw new FieldValidationException("projectIds", "Progetto non valido");
            }
            requested.add(internal);
        }
        Map<Long, Project> chosen = projectRepository.findAllById(requested).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));
        if (chosen.size() != requested.size()) {
            throw new FieldValidationException("projectIds", "Progetto non valido");
        }

        List<UserProject> current = userProjectRepository.findByUserIdWithProject(user.getId());
        Set<Long> currentIds = current.stream().map(up -> up.getId().getProjectId()).collect(Collectors.toSet());
        List<Project> added = chosen.values().stream().filter(p -> !currentIds.contains(p.getId())).toList();
        // un progetto disattivato non si assegna; se è già assegnato resta finché l'ADMIN non lo toglie
        added.stream().filter(p -> !p.isActive()).findFirst().ifPresent(p -> {
            throw new FieldValidationException("projectIds",
                    "Il progetto " + p.getName() + " è disattivato: non si può assegnare");
        });
        List<UserProject> removed = current.stream()
                .filter(up -> !requested.contains(up.getId().getProjectId())).toList();
        if (added.isEmpty() && removed.isEmpty()) {
            return toDto(user, current.stream().map(UserProject::getProject).toList());
        }

        userProjectRepository.deleteAll(removed);
        userProjectRepository.saveAll(added.stream().map(p -> new UserProject(user, p)).toList());
        userProjectRepository.flush();
        List<Project> after = List.copyOf(chosen.values());
        auditService.record(AuditEntityType.APP_USER, user.getId(), "PROJECTS_CHANGED", AuditChanges.of()
                .value("projects", codes(current.stream().map(UserProject::getProject).toList()), codes(after)), null);
        log.info("Progetti di {} aggiornati: +{} -{}", user, added.size(), removed.size());
        return toDto(user, after);
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
        return new UserCreatedDTO(toDto(saved, List.of()), invite(saved));
    }

    @Override
    public void resendInvitation(String id) {
        AppUser user = findUser(id);
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

    private AppUser findUser(String id) {
        return Optional.ofNullable(TsidMapper.toInternal(id)).flatMap(appUserRepository::findById)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
    }

    private List<Project> assignedProjects(AppUser user) {
        return userProjectRepository.findByUserIdWithProject(user.getId()).stream().map(UserProject::getProject).toList();
    }

    /** Codici dei progetti in ordine alfabetico, per l'audit. */
    private static List<String> codes(List<Project> projects) {
        return projects.stream().map(Project::getCode).sorted().toList();
    }

    private static UserDTO toDto(AppUser user, List<Project> projects) {
        List<ReferenceItemDTO> items = projects.stream().sorted(BY_NAME)
                .map(p -> new ReferenceItemDTO(TsidMapper.toExternal(p.getId()), p.getCode(), p.getName())).toList();
        return new UserDTO(TsidMapper.toExternal(user.getId()), user.getUsername(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getPhone(), user.getRole(), user.isActive(), items);
    }
}
