package it.nexus.domain;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Progetto finanziato (es. GOL, POLIS, FSE). */
@Entity
@Table(name = "project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends AbstractReferenceEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Il riferimento all'avviso dei progetti che non ne indicano uno: l'Avviso pubblico n. 10 di GOL (US-1011). */
    public static final String DEFAULT_NOTICE_REFERENCE = "Avviso pubblico n. 10 per l’attuazione del Programma "
            + "Garanzia Occupabilità dei Lavoratori da finanziare nell’ambito del Piano Nazionale di Ripresa e "
            + "Resilienza (PNRR), Missione 5 “Inclusione e coesione”, Componente 1 “Politiche per il Lavoro”, "
            + "Riforma 1.1 “Politiche Attive del Lavoro e Formazione”, finanziato dall’Unione europea – Next "
            + "Generation EU";

    /** Tipi di tirocinio ammessi (US-115); vuoto = tutti i tipi, fino al censimento dei progetti. */
    @Getter
    @ManyToMany
    @JoinTable(name = "project_internship_type", joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "internship_type_id"))
    private Set<InternshipType> allowedInternshipTypes = new HashSet<>();

    /**
     * Riferimento all'avviso nei documenti (US-1011): vuoto = {@link #DEFAULT_NOTICE_REFERENCE}, così chi aggiunge un
     * progetto non deve riscriverlo.
     */
    @Getter
    @Setter
    @Column(name = "notice_reference", length = 1000)
    private String noticeReference;

    /** Riga di intestazione dei documenti con codice e CUP (US-1011), es. «Firenze in Rete 2 – Codice … – CUP …». */
    @Getter
    @Setter
    @Column(name = "header_line", length = 300)
    private String headerLine;

    /** Dati del soggetto promotore nei documenti (US-1013); {@code null} quando non ce n'è nessuno. */
    @Embedded
    private ProjectPromoter promoter;

    /** I dati del promotore, mai {@code null}: senza dati, {@link ProjectPromoter#EMPTY}. */
    public ProjectPromoter getPromoter() {
        return promoter == null ? ProjectPromoter.EMPTY : promoter;
    }

    public void setPromoter(ProjectPromoter promoter) {
        this.promoter = ProjectPromoter.EMPTY.equals(promoter) ? null : promoter;
    }

    /** Il riferimento all'avviso da usare nei documenti: quello del progetto oppure il predefinito. */
    public String effectiveNoticeReference() {
        return noticeReference == null ? DEFAULT_NOTICE_REFERENCE : noticeReference;
    }

    /** Nuovo progetto del catalogo (US-102). */
    public Project(String code, String name) {
        setCode(code);
        setName(name);
    }
}
