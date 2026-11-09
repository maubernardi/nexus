package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Versione di una provincia (US-114): valida da {@code validFrom} (compreso) a {@code validTo} (escluso, vuoto = ancora
 * valida). Non si modifica: un cambio chiude questa versione e ne apre un'altra.
 */
@Getter
@Entity
@Table(name = "province")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Province extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Codice ISTAT dell'unità territoriale sovracomunale (3 cifre). */
    @Column(name = "code", nullable = false, length = 3, updatable = false)
    private String code;

    @Column(name = "name", nullable = false, length = 100, updatable = false)
    private String name;

    /** Sigla automobilistica (es. FI). */
    @Column(name = "abbreviation", nullable = false, length = 2, updatable = false)
    private String abbreviation;

    @Column(name = "valid_from", nullable = false, updatable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    public Province(String code, String name, String abbreviation, LocalDate validFrom) {
        this.code = code;
        this.name = name;
        this.abbreviation = abbreviation;
        this.validFrom = validFrom;
    }

    /** Chiude la versione: dal giorno indicato non vale più. */
    public void close(LocalDate day) {
        this.validTo = day;
    }
}
