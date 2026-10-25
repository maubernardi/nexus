package it.nexus.services;

import java.util.Collection;
import java.util.Map;

public interface UserDisplayNameService {

    /** Nome e cognome per ogni username indicato; "Sistema" per gli utenti non censiti (es. dati iniziali). */
    Map<String, String> displayNames(Collection<String> usernames);
}
