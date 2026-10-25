package it.nexus.services.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.repository.AppUserRepository;
import it.nexus.services.UserDisplayNameService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserDisplayNameServiceImpl implements UserDisplayNameService {

    private final AppUserRepository appUserRepository;

    @Override
    public Map<String, String> displayNames(Collection<String> usernames) {
        Map<String, String> names = new HashMap<>();
        appUserRepository.findByUsernameIn(new HashSet<>(usernames))
                .forEach(u -> names.put(u.getUsername(), u.getFirstName() + " " + u.getLastName()));
        usernames.forEach(u -> names.putIfAbsent(u, "Sistema"));
        return names;
    }
}
