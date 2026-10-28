package it.nexus.domain.enumeration;

import java.util.Arrays;
import java.util.Optional;

/** Cataloghi gestiti dall'ADMIN (US-106), con il tratto dell'indirizzo e il tipo di oggetto nell'audit. */
public enum CatalogKind {
    ZONES("zones", AuditEntityType.ZONE, "ZONA"),
    JOB_CATEGORIES("job-categories", AuditEntityType.JOB_CATEGORY, "TIPOLOGIA");

    private final String path;
    private final AuditEntityType auditType;
    private final String fallbackCode;

    CatalogKind(String path, AuditEntityType auditType, String fallbackCode) {
        this.path = path;
        this.auditType = auditType;
        this.fallbackCode = fallbackCode;
    }

    public static Optional<CatalogKind> fromPath(String path) {
        return Arrays.stream(values()).filter(k -> k.path.equals(path)).findFirst();
    }

    public AuditEntityType auditType() {
        return auditType;
    }

    /** Codice di riserva per un nome senza lettere né cifre. */
    public String fallbackCode() {
        return fallbackCode;
    }
}
