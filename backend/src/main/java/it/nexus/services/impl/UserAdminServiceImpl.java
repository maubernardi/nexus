package it.nexus.services.impl;

import java.time.Duration;
import java.util.ArrayList;
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
import it.nexus.domain.Ticket;
import it.nexus.domain.UserProject;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.UserCreateDTO;
import it.nexus.domain.dto.UserCreatedDTO;
import it.nexus.domain.dto.ReferenceItemDTO;
import it.nexus.domain.dto.UserDTO;
import it.nexus.domain.dto.UserImpactDTO;
import it.nexus.domain.dto.UserProjectsDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.CurrentAppUserService;
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
    private final TicketRepository ticketRepository;
    private final CurrentAppUserService currentAppUserService;
    private final IdentityProviderUsers identityProviderUsers;
    private final AuditService auditService;
    private final ApplicationProperties applicationProperties;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> list() {
        Map<Long, List<UserProject>> projectsByUser = userProjectRepository.findAllWithProject().stream()
                .collect(Collectors.groupingBy(up -> up.getId().getUserId()));
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
        Set<Long> requested = projectIds(dto.projectIds(), "projectIds");
        Set<Long> coordinated = projectIds(
                dto.coordinatedProjectIds() == null ? List.of() : dto.coordinatedProjectIds(), "coordinatedProjectIds");
        if (!requested.containsAll(coordinated)) {
            throw new FieldValidationException("coordinatedProjectIds",
                    "Un Tutor coordina solo i progetti a cui è assegnato");
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
        List<UserProject> kept = current.stream().filter(up -> requested.contains(up.getId().getProjectId())).toList();
        List<UserProject> toggled = kept.stream()
                .filter(up -> up.isCoordinator() != coordinated.contains(up.getId().getProjectId())).toList();
        if (added.isEmpty() && removed.isEmpty() && toggled.isEmpty()) {
            return toDto(user, current);
        }

        List<String> projectsBefore = codes(current, false);
        List<String> coordinatedBefore = codes(current, true);
        toggled.forEach(up -> up.setCoordinator(!up.isCoordinator()));
        List<UserProject> created = added.stream().map(p -> {
            UserProject up = new UserProject(user, p);
            up.setCoordinator(coordinated.contains(p.getId()));
            return up;
        }).toList();
        userProjectRepository.deleteAll(removed);
        userProjectRepository.saveAll(created);
        userProjectRepository.flush();
        List<UserProject> after = new ArrayList<>(kept);
        after.addAll(created);
        auditService.record(AuditEntityType.APP_USER, user.getId(), "PROJECTS_CHANGED", AuditChanges.of()
                .value("projects", projectsBefore, codes(after, false))
                .value("coordinated", coordinatedBefore, codes(after, true)), null);
        log.info("Progetti di {} aggiornati: +{} -{}, coordinamento cambiato su {}", user, added.size(), removed.size(),
                toggled.size());
        return toDto(user, after);
    }

    private static Set<Long> projectIds(List<String> ids, String field) {
        Set<Long> result = new LinkedHashSet<>();
        for (String projectId : ids) {
            Long internal = TsidMapper.toInternal(projectId);
            if (internal == null) {
                throw new FieldValidationException(field, "Progetto non valido");
            }
            result.add(internal);
        }
        return result;
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

    @Override
    @Transactional(readOnly = true)
    public UserImpactDTO impact(String id) {
        AppUser user = findUser(id);
        return new UserImpactDTO(
                ticketRepository.countByTutorIdAndStatusNot(user.getId(), TicketStatus.CLOSED),
                ticketRepository.findByAssignedCcOperatorIdAndStatusNot(user.getId(), TicketStatus.CLOSED).size(),
                isMe(user));
    }

    @Override
    public UserDTO setActive(String id, boolean active) {
        AppUser user = findUser(id);
        if (user.isActive() == active) {
            return toDto(user, assignedProjects(user));
        }
        if (isMe(user)) {
            throw new BadRequestException("Non puoi disattivare il tuo utente: chiedi a un altro ADMIN");
        }
        AuditChanges changes = AuditChanges.of().value("active", !active, active);
        if (!active) {
            requireNoOpenTickets(user, "disattivarlo");
            changes.merge(released(release(user)));
        }
        user.setActive(active);
        appUserRepository.saveAndFlush(user);
        auditService.record(AuditEntityType.APP_USER, user.getId(), active ? "REACTIVATE" : "DEACTIVATE", changes, null);
        // Keycloak per ultimo: se non risponde la transazione si annulla e in NEXUS non cambia nulla
        try {
            identityProviderUsers.setEnabled(user.getExternalId(), active);
        } catch (IdentityProviderException e) {
            throw new ServiceUnavailableException(
                    "Non è stato possibile aggiornare l'account di accesso: l'utente non è cambiato, riprova più tardi");
        }
        if (!active) {
            logoutQuietly(user);
        }
        log.info("Utente {} {}", user, active ? "riattivato" : "disattivato");
        return toDto(user, assignedProjects(user));
    }

    @Override
    public UserDTO changeRole(String id, Role role) {
        AppUser user = findUser(id);
        Role before = user.getRole();
        if (before == role) {
            return toDto(user, assignedProjects(user));
        }
        if (isMe(user)) {
            throw new BadRequestException("Non puoi cambiare il tuo ruolo: chiedi a un altro ADMIN");
        }
        requireNoOpenTickets(user, "cambiargli il ruolo");
        AuditChanges changes = AuditChanges.of().value("role", before.name(), role.name());
        // un Tutor non lavora i ticket del Call Center: quelli in carico tornano in coda
        if (role == Role.TUTOR) {
            changes.merge(released(release(user)));
        }
        // il coordinamento è un permesso in più del Tutor (US-112): con un altro ruolo non resta in sospeso
        if (before == Role.TUTOR) {
            List<UserProject> coordinated = userProjectRepository.findCoordinatedBy(user.getId());
            changes.value("coordinated", codes(coordinated, false), List.of());
            coordinated.forEach(up -> up.setCoordinator(false));
        }
        user.setRole(role);
        appUserRepository.saveAndFlush(user);
        auditService.record(AuditEntityType.APP_USER, user.getId(), "ROLE_CHANGE", changes, null);
        try {
            identityProviderUsers.changeRole(user.getExternalId(), role);
        } catch (IdentityProviderException e) {
            throw new ServiceUnavailableException(
                    "Non è stato possibile aggiornare l'account di accesso: il ruolo non è cambiato, riprova più tardi");
        }
        logoutQuietly(user);
        log.info("Ruolo di {} cambiato: {} -> {}", user, before, role);
        return toDto(user, assignedProjects(user));
    }

    private boolean isMe(AppUser user) {
        return user.getId().equals(currentAppUserService.getCurrentAppUser().getId());
    }

    /** Le segnalazioni aperte di un Tutor non restano senza nessuno: prima si spostano (US-111, decisione del PO). */
    private void requireNoOpenTickets(AppUser user, String action) {
        long open = ticketRepository.countByTutorIdAndStatusNot(user.getId(), TicketStatus.CLOSED);
        if (open > 0) {
            throw new BadRequestException(("Ha %d segnalazion%s apert%s: prima di %s spostale su un altro Tutor dello "
                    + "stesso progetto, con il pulsante Segnalazioni dell'elenco").formatted(open,
                    open == 1 ? "e" : "i", open == 1 ? "a" : "e", action));
        }
    }

    /** I ticket aperti in carico all'utente tornano liberi nella coda del Call Center (decisione del PO, 06/11). */
    private int release(AppUser user) {
        List<Ticket> inCharge = ticketRepository.findByAssignedCcOperatorIdAndStatusNot(user.getId(), TicketStatus.CLOSED);
        String operatorId = TsidMapper.toExternal(user.getId());
        for (Ticket ticket : inCharge) {
            ticket.setAssignedCcOperator(null);
            auditService.record(AuditEntityType.TICKET, ticket.getId(), "RELEASE",
                    AuditChanges.of().value("assignedCcOperatorId", operatorId, null), null);
        }
        ticketRepository.saveAll(inCharge);
        return inCharge.size();
    }

    private static AuditChanges released(int count) {
        return count == 0 ? null : AuditChanges.of().value("releasedTickets", null, count);
    }

    /** Chiudere le sessioni è un di più: l'account è già aggiornato e il token vecchio scade in pochi minuti. */
    private void logoutQuietly(AppUser user) {
        try {
            identityProviderUsers.logout(user.getExternalId());
        } catch (IdentityProviderException e) {
            log.warn("Sessioni di {} non chiuse: {}", user, e.getMessage());
        }
    }

    private AppUser findUser(String id) {
        return Optional.ofNullable(TsidMapper.toInternal(id)).flatMap(appUserRepository::findById)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
    }

    private List<UserProject> assignedProjects(AppUser user) {
        return userProjectRepository.findByUserIdWithProject(user.getId());
    }

    /** Codici dei progetti assegnati (o solo coordinati) in ordine alfabetico, per l'audit. */
    private static List<String> codes(List<UserProject> assignments, boolean onlyCoordinated) {
        return assignments.stream().filter(up -> !onlyCoordinated || up.isCoordinator())
                .map(up -> up.getProject().getCode()).sorted().toList();
    }

    private static UserDTO toDto(AppUser user, List<UserProject> assignments) {
        List<UserProject> sorted = assignments.stream().sorted(Comparator.comparing(UserProject::getProject, BY_NAME))
                .toList();
        return new UserDTO(TsidMapper.toExternal(user.getId()), user.getUsername(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getPhone(), user.getRole(), user.isActive(),
                sorted.stream().map(UserAdminServiceImpl::item).toList(),
                sorted.stream().filter(UserProject::isCoordinator).map(UserAdminServiceImpl::item).toList());
    }

    private static ReferenceItemDTO item(UserProject assignment) {
        Project p = assignment.getProject();
        return new ReferenceItemDTO(TsidMapper.toExternal(p.getId()), p.getCode(), p.getName());
    }
}
