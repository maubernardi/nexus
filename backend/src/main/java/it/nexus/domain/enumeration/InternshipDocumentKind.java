package it.nexus.domain.enumeration;

import java.util.Arrays;
import java.util.Optional;

/**
 * Documenti del tirocinio generati dai modelli Word (US-1004, US-1005). {@code slug} è il nome nell'URL e la cartella dei
 * modelli ({@code templates/documents/<slug>/}); {@code label} va nella cronologia, {@code filePrefix} nel nome del
 * file scaricato.
 */
public enum InternshipDocumentKind {
    CONVENZIONE("convenzione", "Convenzione", "Convenzione"),
    PROGETTO_FORMATIVO("progetto-formativo", "Progetto Formativo", "Progetto-formativo");

    private final String slug;
    private final String label;
    private final String filePrefix;

    InternshipDocumentKind(String slug, String label, String filePrefix) {
        this.slug = slug;
        this.label = label;
        this.filePrefix = filePrefix;
    }

    public String filePrefix() {
        return filePrefix;
    }

    public String slug() {
        return slug;
    }

    public String label() {
        return label;
    }

    public static Optional<InternshipDocumentKind> fromSlug(String slug) {
        return Arrays.stream(values()).filter(k -> k.slug.equals(slug)).findFirst();
    }
}
