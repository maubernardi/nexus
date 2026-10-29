package it.nexus.domain.enumeration;

import java.util.Arrays;
import java.util.Optional;

/**
 * Documenti del tirocinio generati dai modelli Word (US-1004). {@code slug} è il nome nell'URL e la cartella dei
 * modelli ({@code templates/documents/<slug>/}); {@code label} va nel nome del file e nella cronologia.
 */
public enum InternshipDocumentKind {
    CONVENZIONE("convenzione", "Convenzione");

    private final String slug;
    private final String label;

    InternshipDocumentKind(String slug, String label) {
        this.slug = slug;
        this.label = label;
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
