package it.nexus.domain;

import java.io.Serial;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import it.nexus.domain.enumeration.LanguageLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Lingua conosciuta da un candidato (ISO 639-1) con livello QCER. */
@Getter
@Setter
@Entity
@Table(name = "candidate_language")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateLanguage extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "language", nullable = false, length = 2)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 12)
    private LanguageLevel level;

    public CandidateLanguage(String language, LanguageLevel level) {
        this.language = language;
        this.level = level;
    }
}
