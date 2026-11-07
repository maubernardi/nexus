package it.nexus.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AppUser;
import it.nexus.domain.dto.AccessLogEntryDTO;
import it.nexus.domain.dto.AccessLogPageDTO;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AppUserRepository;
import it.nexus.services.AccessLogService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.identity.IdentityProviderException;
import it.nexus.services.identity.IdentityProviderUsers;
import it.nexus.services.identity.LoginEvent;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.NotFoundException;
import it.nexus.web.errors.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccessLogServiceImpl implements AccessLogService {

    /** Oltre non si va: 12 mesi di accessi stanno ampiamente in 100 pagine. */
    static final int MAX_PAGE = 100;

    private final IdentityProviderUsers identityProviderUsers;
    private final AppUserRepository appUserRepository;
    private final CurrentAppUserService currentAppUserService;

    @Override
    public AccessLogPageDTO forUser(String userId, int page) {
        AppUser user = Optional.ofNullable(TsidMapper.toInternal(userId)).flatMap(appUserRepository::findById)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));
        return ofUser(user, page);
    }

    @Override
    public AccessLogPageDTO mine(int page) {
        return ofUser(currentAppUserService.getCurrentAppUser(), page);
    }

    @Override
    public AccessLogPageDTO failed(int page) {
        List<LoginEvent> events = read(page, (first, max) -> identityProviderUsers.failedLogins(first, max));
        Map<String, AppUser> users = appUserRepository.findByExternalIdIn(events.stream()
                        .map(LoginEvent::userExternalId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(AppUser::getExternalId, Function.identity()));
        return page(events, e -> users.get(e.userExternalId()));
    }

    private AccessLogPageDTO ofUser(AppUser user, int page) {
        return page(read(page, (first, max) -> identityProviderUsers.loginEvents(user.getExternalId(), first, max)),
                e -> user);
    }

    private interface Reader {
        List<LoginEvent> read(int first, int max);
    }

    /** Una riga in più dice se c'è un'altra pagina. */
    private static List<LoginEvent> read(int page, Reader reader) {
        if (page < 0 || page >= MAX_PAGE) {
            throw new BadRequestException("Pagina non valida");
        }
        try {
            return reader.read(page * PAGE_SIZE, PAGE_SIZE + 1);
        } catch (IdentityProviderException e) {
            throw new ServiceUnavailableException("Il registro degli accessi non è disponibile: riprova più tardi");
        }
    }

    private static AccessLogPageDTO page(List<LoginEvent> events, Function<LoginEvent, AppUser> userOf) {
        List<AccessLogEntryDTO> entries = events.stream().limit(PAGE_SIZE).map(e -> {
            AppUser user = userOf.apply(e);
            return new AccessLogEntryDTO(e.at(), e.success(), e.ipAddress(), e.username(),
                    user == null ? null : user.getFirstName() + " " + user.getLastName(), e.error());
        }).toList();
        return new AccessLogPageDTO(entries, events.size() > PAGE_SIZE);
    }
}
