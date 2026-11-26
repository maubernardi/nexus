package it.nexus.legacy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simboli del database attuale all'inizio del nome di un'azienda (US-514, PO 25/11): ogni {@code $} è una stella di
 * affidabilità (al massimo 4: cinque {@code $} valgono 4 stelle), ogni {@code £} un pollice di interesse al progetto (al
 * massimo 2); {@code &} e {@code ^} sono refusi. In qualunque ordine, anche separati da spazi; la {@code &} dentro il
 * nome resta. La stessa regola è nella migrazione V52, che converte le aziende già importate: i test usano gli stessi
 * esempi.
 *
 * @param name nome senza i simboli; {@code null} se non resta nulla
 * @param reliability stelle, {@code null} se nessuna
 * @param projectInterest pollici, {@code null} se nessuno
 * @param marked se il nome aveva dei simboli all'inizio
 */
record LegacyCompanyMarks(String name, Integer reliability, Integer projectInterest, boolean marked) {

    private static final Pattern PREFIX = Pattern.compile("^[\\s$£&^]+");
    private static final int MAX_RELIABILITY = 4;
    private static final int MAX_PROJECT_INTEREST = 2;

    /** Simboli del nome {@code name}, già ripulito dagli spazi intorno; {@code null} resta {@code null}. */
    static LegacyCompanyMarks of(String name) {
        if (name == null) {
            return new LegacyCompanyMarks(null, null, null, false);
        }
        Matcher prefix = PREFIX.matcher(name);
        if (!prefix.find() || prefix.group().isBlank()) {
            return new LegacyCompanyMarks(name, null, null, false);
        }
        String marks = prefix.group();
        return new LegacyCompanyMarks(LegacyText.clean(name.substring(prefix.end())),
                count(marks, '$', MAX_RELIABILITY), count(marks, '£', MAX_PROJECT_INTEREST), true);
    }

    private static Integer count(String marks, char mark, int max) {
        int n = (int) marks.chars().filter(c -> c == mark).count();
        return n == 0 ? null : Math.min(n, max);
    }
}
