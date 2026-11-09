package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Versione di un comune (US-114), identificato dal codice catastale: valida da {@code validFrom} (compreso) a
 * {@code validTo} (escluso, vuoto = ancora valido). Un cambio di nome, codice ISTAT o provincia chiude questa versione e
 * ne apre un'altra, così i dati che puntano a questa restano com'erano.
 */
@Getter
@Entity
@Table(name = "municipality")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Municipality extends AbstractTsidEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "cadastral_code", nullable = false, length = 4, updatable = false)
    private String cadastralCode;

    @Column(name = "istat_code", nullable = false, length = 6, updatable = false)
    private String istatCode;

    @Column(name = "name", nullable = false, length = 150, updatable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "province_id", nullable = false, updatable = false)
    private Province province;

    @Column(name = "valid_from", nullable = false, updatable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    public Municipality(String cadastralCode, String istatCode, String name, Province province, LocalDate validFrom) {
        this.cadastralCode = cadastralCode;
        this.istatCode = istatCode;
        this.name = name;
        this.province = province;
        this.validFrom = validFrom;
    }

    /** Chiude la versione: dal giorno indicato non vale più. */
    public void close(LocalDate day) {
        this.validTo = day;
    }
}
