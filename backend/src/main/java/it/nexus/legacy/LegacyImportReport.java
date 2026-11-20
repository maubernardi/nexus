package it.nexus.legacy;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import it.nexus.mapper.TsidMapper;

/**
 * Resoconto dell'importazione dal database attuale (US-1309). Contiene solo conteggi, numeri di Access, collegamenti
 * alle schede e testi dei comuni: niente nomi di aziende o di persone, recapiti o note.
 */
public final class LegacyImportReport {

    private static final DateTimeFormatter WHEN =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Europe/Rome"));

    final Instant startedAt;
    int companiesRead;
    int companiesAdded;
    int unnamed;
    final Map<MunicipalityMatcher.Rule, Integer> legalByRule = new EnumMap<>(MunicipalityMatcher.Rule.class);
    final Map<MunicipalityMatcher.Rule, Integer> operatingByRule = new EnumMap<>(MunicipalityMatcher.Rule.class);
    int categories;
    int unknownCategories;
    int contactsRead;
    int contactsAdded;
    int unknownRoles;
    int contactDetailNotes;
    int notesRead;
    int notesAdded;
    int callsRead;
    int callsWithoutNumber;
    int callsAdded;
    int callsWithCompany;
    /** Testo del comune non trovato → numeri delle aziende. */
    final Map<String, List<Integer>> unmatched = new TreeMap<>();
    final List<List<Integer>> duplicates = new ArrayList<>();
    final List<Integer> unnamedNumbers = new ArrayList<>();
    final List<Integer> callsToCheck = new ArrayList<>();

    LegacyImportReport(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public int companiesAdded() {
        return companiesAdded;
    }

    public int callsAdded() {
        return callsAdded;
    }

    /** Riga per il log: solo conteggi. */
    public String summary() {
        return ("Importazione dal database attuale: aziende %d lette, %d nuove; referenti %d nuovi; note %d nuove; "
                + "chiamate %d lette, %d nuove; comuni non trovati %d").formatted(companiesRead, companiesAdded,
                contactsAdded, notesAdded, callsRead, callsAdded, unmatchedCount());
    }

    private int unmatchedCount() {
        return unmatched.values().stream().mapToInt(List::size).sum();
    }

    public String toText() {
        StringBuilder out = new StringBuilder();
        out.append("Importazione dal database attuale (US-1309) — ").append(WHEN.format(startedAt)).append("\n\n");
        out.append("## Conteggi\n\n");
        line(out, "Aziende lette", companiesRead);
        line(out, "Aziende nuove (le altre c'erano già)", companiesAdded);
        line(out, "  senza nome («Azienda senza nome n. …»)", unnamed);
        out.append("- Comune della sede legale abbinato: ").append(rules(legalByRule)).append('\n');
        out.append("- Comune della sede operativa abbinato: ").append(rules(operatingByRule)).append('\n');
        line(out, "Comuni non trovati (sede legale e operativa)", unmatchedCount());
        line(out, "Aziende con la categoria", categories);
        line(out, "Categorie sconosciute (azienda senza categoria)", unknownCategories);
        line(out, "Referenti letti", contactsRead);
        line(out, "Referenti nuovi", contactsAdded);
        line(out, "  con un ruolo sconosciuto (entrati senza ruolo)", unknownRoles);
        line(out, "Aziende con recapiti fuori formato (nota «Recapiti del database attuale»)", contactDetailNotes);
        line(out, "Note di Access", notesRead);
        line(out, "Note nuove nel diario (con i recapiti fuori formato)", notesAdded);
        line(out, "Chiamate lette", callsRead);
        line(out, "  senza numero (non importate)", callsWithoutNumber);
        line(out, "Chiamate nuove nell'archivio", callsAdded);
        line(out, "  collegate a un'azienda", callsWithCompany);

        out.append("\n## Da rivedere\n\n");
        out.append("### Duplicati per nome (").append(duplicates.size()).append(" gruppi)\n\n");
        duplicates.forEach(group -> out.append("- ").append(group.stream().map(LegacyImportReport::company)
                .collect(Collectors.joining(", "))).append('\n'));
        out.append("\n### Comuni non trovati (").append(unmatched.size()).append(" testi)\n\n");
        unmatched.forEach((text, numbers) -> out.append("- «").append(text).append("» (").append(numbers.size())
                .append("): ").append(numbers.stream().map(LegacyImportReport::company)
                        .collect(Collectors.joining(", "))).append('\n'));
        out.append("\n### Aziende senza nome (").append(unnamedNumbers.size()).append(")\n\n");
        unnamedNumbers.forEach(n -> out.append("- ").append(company(n)).append('\n'));
        out.append("\n### Chiamate con possibili nomi di beneficiari (").append(callsToCheck.size()).append(")\n\n");
        out.append("Note con «utente», «candidato» o «ragazzo» seguiti da due parole con l'iniziale maiuscola: un aiuto "
                + "al controllo, che non lo sostituisce.\n\n");
        callsToCheck.forEach(n -> out.append("- chiamata n. ").append(n).append('\n'));
        return out.toString();
    }

    private static void line(StringBuilder out, String label, int value) {
        out.append(label.startsWith("  ") ? "  - " + label.strip() : "- " + label).append(": ").append(value)
                .append('\n');
    }

    private static String rules(Map<MunicipalityMatcher.Rule, Integer> byRule) {
        int total = byRule.values().stream().mapToInt(Integer::intValue).sum();
        return total + " (" + byRule.entrySet().stream().map(e -> label(e.getKey()) + " " + e.getValue())
                .collect(Collectors.joining(", ")) + ")";
    }

    private static String label(MunicipalityMatcher.Rule rule) {
        return switch (rule) {
            case ALIAS -> "alias";
            case EXACT -> "nome esatto";
            case PROVINCE -> "nome e sigla";
            case PREFIX -> "parole iniziali";
            case TRUNCATED -> "nome tagliato";
        };
    }

    /** «n. 123 (/aziende/0ABC…)»: il numero di Access e la scheda in NEXUS. */
    private static String company(int number) {
        return "n. " + number + " (/aziende/" + TsidMapper.toExternal(LegacyIds.company(number)) + ")";
    }
}
