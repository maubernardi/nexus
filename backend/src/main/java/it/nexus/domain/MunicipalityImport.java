package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

import it.nexus.domain.id.TsidId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Caricamento confermato del file ISTAT (US-114): data da cui vale e conteggi delle differenze. Non si modifica. */
@Getter
@Entity
@Table(name = "municipality_import")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MunicipalityImport extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @Column(name = "valid_from", nullable = false, updatable = false)
    private LocalDate validFrom;

    @Column(name = "municipalities_added", nullable = false, updatable = false)
    private int municipalitiesAdded;

    @Column(name = "municipalities_removed", nullable = false, updatable = false)
    private int municipalitiesRemoved;

    @Column(name = "municipalities_changed", nullable = false, updatable = false)
    private int municipalitiesChanged;

    @Column(name = "provinces_added", nullable = false, updatable = false)
    private int provincesAdded;

    @Column(name = "provinces_removed", nullable = false, updatable = false)
    private int provincesRemoved;

    @Column(name = "provinces_changed", nullable = false, updatable = false)
    private int provincesChanged;

    @SuppressWarnings("java:S107") // un caricamento è proprio questi sei conteggi
    public MunicipalityImport(LocalDate validFrom, int municipalitiesAdded, int municipalitiesRemoved,
            int municipalitiesChanged, int provincesAdded, int provincesRemoved, int provincesChanged) {
        this.validFrom = validFrom;
        this.municipalitiesAdded = municipalitiesAdded;
        this.municipalitiesRemoved = municipalitiesRemoved;
        this.municipalitiesChanged = municipalitiesChanged;
        this.provincesAdded = provincesAdded;
        this.provincesRemoved = provincesRemoved;
        this.provincesChanged = provincesChanged;
    }

    @Override
    public String toString() {
        return "MunicipalityImport[validFrom=" + validFrom + "]";
    }
}
