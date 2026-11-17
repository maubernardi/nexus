package it.nexus.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Dati del soggetto promotore di un progetto, usati nei documenti (US-1013, al posto di {@code NEXUS_PROMOTER_*}).
 * Tutti facoltativi: un dato che manca resta, nei documenti, una riga da completare. Contiene dati personali del
 * legale rappresentante: mai nei log.
 */
@Embeddable
public record ProjectPromoter(
        @Column(name = "promoter_name", length = 300) String name,
        @Column(name = "promoter_address", length = 300) String address,
        @Column(name = "promoter_fiscal_code", length = 16) String fiscalCode,
        @Column(name = "promoter_representative", length = 200) String representative,
        @Column(name = "promoter_representative_role", length = 200) String representativeRole,
        @Column(name = "promoter_representative_birth_place", length = 200) String representativeBirthPlace,
        @Column(name = "promoter_representative_birth_date") LocalDate representativeBirthDate,
        @Column(name = "promoter_phone", length = 50) String phone,
        @Column(name = "promoter_email", length = 254) String email,
        @Column(name = "promoter_pec", length = 254) String pec,
        @Column(name = "promoter_inail_position", length = 100) String inailPosition,
        @Column(name = "promoter_rc_insurer", length = 200) String rcInsurer,
        @Column(name = "promoter_rc_policy", length = 100) String rcPolicy,
        @Column(name = "promoter_rc_expiry") LocalDate rcExpiry) {

    /** Nessun dato: Hibernate carica così un promotore con tutte le colonne vuote. */
    public static final ProjectPromoter EMPTY = new ProjectPromoter(null, null, null, null, null, null, null, null,
            null, null, null, null, null, null);

    @Override
    public String toString() {
        return "ProjectPromoter";
    }
}
