package it.nexus.domain.audit;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Modifiche registrate in un evento di audit: per ogni campo i valori prima/dopo oppure, per i dati personali, solo il
 * fatto che il campo è stato toccato (mai il valore, per il GDPR). I valori vanno passati già in forma esterna (id TSID
 * come stringa, nomi di enum).
 */
public final class AuditChanges {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    private AuditChanges() {
    }

    public static AuditChanges none() {
        return new AuditChanges();
    }

    public static AuditChanges of() {
        return new AuditChanges();
    }

    /** Valore in chiaro; ignorato se prima e dopo coincidono. {@code null} = assente. */
    public AuditChanges value(String field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            Map<String, Object> change = new LinkedHashMap<>();
            change.put("before", before);
            change.put("after", after);
            fields.put(field, change);
        }
        return this;
    }

    /** Dato personale: si registra solo che il campo è stato toccato. */
    public AuditChanges redacted(String field) {
        fields.put(field, Map.of("redacted", true));
        return this;
    }

    /** Aggiunge le modifiche di {@code other} (a parità di campo prevale {@code other}). */
    public AuditChanges merge(AuditChanges other) {
        if (other != null) {
            fields.putAll(other.fields);
        }
        return this;
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(fields);
    }
}
