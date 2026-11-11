package it.nexus.services.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.nexus.domain.dto.MunicipalityDTO;

/** Indirizzi su una riga nei documenti (US-1007). */
class AddressLineTest {

    private static final MunicipalityDTO SESTO = new MunicipalityDTO("M1", "Sesto Fiorentino", "048043", "I684",
            "Firenze", "FI", LocalDate.of(2026, 1, 1), null);

    @Test
    void indirizzoEComune() {
        assertThat(AddressLine.of("Via Roma 1", SESTO)).isEqualTo("Via Roma 1, Sesto Fiorentino (FI)");
    }

    @Test
    void residenzaConViaENumeroCivico() {
        assertThat(AddressLine.of("Via Gramsci", "12", SESTO)).isEqualTo("Via Gramsci 12, Sesto Fiorentino (FI)");
        assertThat(AddressLine.of("Via Gramsci", null, SESTO)).isEqualTo("Via Gramsci, Sesto Fiorentino (FI)");
    }

    @Test
    void partiMancantiSenzaSeparatori() {
        assertThat(AddressLine.of("Via Roma 1", null)).isEqualTo("Via Roma 1");
        assertThat(AddressLine.of("  ", SESTO)).isEqualTo("Sesto Fiorentino (FI)");
        assertThat(AddressLine.of(null, " ", null)).isNull();
    }
}
