package it.nexus.legacy;

import java.util.List;

/**
 * Esempi della regola dei simboli all'inizio del nome (US-514, spec company-registry e legacy-import): li usano sia il
 * test della classe Java dell'importazione sia quello della migrazione V52, così le due regole non divergono.
 */
final class LegacyMarksExamples {

    /** Nome nel database attuale → nome ripulito, stelle e pollici attesi ({@code null} = nessuno). */
    record Example(String name, String cleanName, Integer reliability, Integer projectInterest) {
    }

    static final List<Example> NAMED = List.of(
            new Example("£$$ Officina Rossi", "Officina Rossi", 2, 1),
            new Example("$$£ Officina Bianchi", "Officina Bianchi", 2, 1),
            new Example("$ Bottega Gialli", "Bottega Gialli", 1, null),
            new Example("$$$ Studio Viola", "Studio Viola", 3, null),
            new Example("$$$$$ Bar Centrale", "Bar Centrale", 4, null),
            new Example("£ $$ Studio Neri", "Studio Neri", 2, 1),
            new Example("££$ Pasticceria Blu", "Pasticceria Blu", 1, 2),
            new Example("£Forno Rosa", "Forno Rosa", null, 1),
            new Example("&Ferramenta Bianchi", "Ferramenta Bianchi", null, null),
            new Example("£& Forno Verdi", "Forno Verdi", null, 1),
            new Example("^Tipografia Neri", "Tipografia Neri", null, null),
            new Example("Rossi & C. S.n.c.", "Rossi & C. S.n.c.", null, null),
            new Example("Hotel $ Lusso", "Hotel $ Lusso", null, null));

    /** Nome fatto solo di simboli: diventa il segnaposto dei nomi da completare. */
    static final Example ONLY_MARKS = new Example("$$", null, 2, null);

    private LegacyMarksExamples() {
    }
}
