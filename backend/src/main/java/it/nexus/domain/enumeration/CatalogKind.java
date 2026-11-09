package it.nexus.domain.enumeration;

import java.util.Arrays;
import java.util.Optional;

/** Cataloghi gestiti dall'ADMIN (US-106, progetti US-102), con il tratto dell'indirizzo e il tipo di oggetto nell'audit. */
public enum CatalogKind {
    ZONES("zones", AuditEntityType.ZONE, "ZONA"),
    JOB_CATEGORIES("job-categories", AuditEntityType.JOB_CATEGORY, "TIPOLOGIA"),
    INTERNSHIP_TYPES("internship-types", AuditEntityType.INTERNSHIP_TYPE, "TIPO"),
    /** Report di restituzione (US-109). */
    HIRING_FORMULAS("hiring-formulas", AuditEntityType.HIRING_FORMULA, "FORMULA"),
    NO_HIRE_REASONS("no-hire-reasons", AuditEntityType.NO_HIRE_REASON, "MOTIVO"),
    /** Progetti finanziati (US-102). */
    PROJECTS("projects", AuditEntityType.PROJECT, "PROGETTO"),
    /** Sedi di competenza e enti di presa in carico (US-115). */
    COMPETENCE_SITES("competence-sites", AuditEntityType.COMPETENCE_SITE, "SEDE"),
    CARE_AGENCIES("care-agencies", AuditEntityType.CARE_AGENCY, "ENTE");

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
