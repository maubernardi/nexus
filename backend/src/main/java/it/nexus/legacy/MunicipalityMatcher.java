package it.nexus.legacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Abbina il comune scritto nel database attuale a un comune vigente dell'elenco ufficiale (US-1309). Regole, in
 * ordine: alias, nome esatto, nome con la sigla della provincia (in coda o in testa), parole iniziali che sono un
 * comune, testo tagliato a 20 caratteri che è l'inizio di un solo comune. La sigla sceglie tra i comuni con lo stesso
 * nome.
 */
public final class MunicipalityMatcher {

    /** Lunghezza dei campi del comune in Access: un testo così lungo può essere un nome tagliato. */
    static final int ACCESS_FIELD_LENGTH = 20;
    /**
     * Province toscane: il database attuale è del Call Center di Firenze. Le parole iniziali che sono anche l'inizio di
     * un comune più lungo («SESTO» di «Sesto Fiorentino», «PONTE» di «Ponte a Signa») valgono solo per un comune di
     * queste province, come Prato in «PRATO CALENZANO».
     */
    static final Set<String> HOME_PROVINCES = Set.of("FI", "PO", "PT", "AR", "SI", "PI", "LU", "LI", "GR", "MS");

    /** Comune vigente: id, nome e sigla della provincia. */
    public record Candidate(long id, String name, String province) {
    }

    public enum Rule { ALIAS, EXACT, PROVINCE, PREFIX, TRUNCATED }

    public record Match(long id, Rule rule) {
    }

    private final Map<String, List<Candidate>> byName = new HashMap<>();
    private final Set<String> provinces = new HashSet<>();
    private final Map<String, String> aliases = new HashMap<>();

    /** @param aliases testo del database attuale → nome ufficiale del comune, in qualunque forma */
    public MunicipalityMatcher(List<Candidate> municipalities, Map<String, String> aliases) {
        municipalities.forEach(m -> {
            byName.computeIfAbsent(LegacyText.normalize(m.name()), k -> new ArrayList<>()).add(m);
            provinces.add(m.province());
        });
        aliases.forEach((alias, name) -> this.aliases.put(LegacyText.normalize(alias), LegacyText.normalize(name)));
    }

    public boolean isEmpty() {
        return byName.isEmpty();
    }

    public Optional<Match> match(String text) {
        String normalized = LegacyText.normalize(text);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        List<String> words = new ArrayList<>(Arrays.asList(normalized.split(" ")));
        // sigla della provincia in coda («SCANDICCI FI») o in testa («FI SCANDICCI»)
        String province = null;
        if (words.size() > 1 && provinces.contains(words.getLast())) {
            province = words.removeLast();
        } else if (words.size() > 1 && provinces.contains(words.getFirst())) {
            province = words.removeFirst();
        }
        String name = String.join(" ", words);

        String alias = aliases.getOrDefault(normalized, aliases.get(name));
        if (alias != null) {
            return pick(alias, province).map(c -> new Match(c.id(), Rule.ALIAS));
        }
        Optional<Candidate> exact = pick(normalized, null);
        if (exact.isPresent()) {
            return exact.map(c -> new Match(c.id(), Rule.EXACT));
        }
        if (province != null) {
            Optional<Candidate> withProvince = pick(name, province);
            if (withProvince.isPresent()) {
                return withProvince.map(c -> new Match(c.id(), Rule.PROVINCE));
            }
        }
        for (int n = words.size() - 1; n >= 1; n--) {
            String start = String.join(" ", words.subList(0, n));
            Optional<Candidate> prefix = pick(start, province)
                    .filter(c -> HOME_PROVINCES.contains(c.province()) || !startsLongerName(start));
            if (prefix.isPresent()) {
                return prefix.map(c -> new Match(c.id(), Rule.PREFIX));
            }
        }
        if (text.strip().length() >= ACCESS_FIELD_LENGTH) {
            List<Candidate> started = byName.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(normalized))
                    .flatMap(e -> e.getValue().stream())
                    .toList();
            if (started.size() == 1) {
                return Optional.of(new Match(started.getFirst().id(), Rule.TRUNCATED));
            }
        }
        return Optional.empty();
    }

    private boolean startsLongerName(String start) {
        return byName.keySet().stream().anyMatch(name -> name.startsWith(start + " "));
    }

    /** Il comune con quel nome; con la sigla, solo se è di quella provincia (decide anche tra i nomi uguali). */
    private Optional<Candidate> pick(String normalizedName, String province) {
        List<Candidate> candidates = byName.getOrDefault(normalizedName, List.of());
        if (province != null) {
            candidates = candidates.stream().filter(c -> c.province().equals(province)).toList();
        }
        return candidates.size() == 1 ? Optional.of(candidates.getFirst()) : Optional.empty();
    }
}
