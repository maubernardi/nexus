package it.nexus.domain;

import java.io.Serial;

import it.nexus.domain.enumeration.InternshipDocumentKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Modello Word di un documento del tirocinio caricato dall'ADMIN (US-1010). Le condizioni (documento, progetto, tipo,
 * ente) non cambiano dopo il caricamento; il file si sostituisce e il modello si disattiva. Senza dati personali.
 */
@Getter
@Entity
@Table(name = "document_template")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentTemplate extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_kind", nullable = false, updatable = false, length = 30)
    private InternshipDocumentKind kind;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_type_id", nullable = false, updatable = false)
    private InternshipType internshipType;

    /** Nullo = vale per tutti gli enti; con l'ente vale più di quelli senza. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_agency_id", updatable = false)
    private CareAgency careAgency;

    @Column(name = "name", nullable = false, updatable = false, length = 200)
    private String name;

    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false, unique = true)
    private StoredFile file;

    @Setter
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public DocumentTemplate(InternshipDocumentKind kind, Project project, InternshipType internshipType,
            CareAgency careAgency, String name, StoredFile file) {
        this.kind = kind;
        this.project = project;
        this.internshipType = internshipType;
        this.careAgency = careAgency;
        this.name = name;
        this.file = file;
    }
}
