package it.nexus.services.documents;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import it.nexus.domain.dto.CompanyTutorDTO;

/** Tutor aziendali su una riga nei documenti (US-1009). */
class CompanyTutorLineTest {

    @Test
    void nomeRuoloERecapito() {
        assertThat(CompanyTutorLine.of(new CompanyTutorDTO("Mario Rossi", "Responsabile di produzione", "055 123456")))
                .isEqualTo("Mario Rossi, Responsabile di produzione (055 123456)");
    }

    @Test
    void partiMancantiSenzaSeparatori() {
        assertThat(CompanyTutorLine.of(new CompanyTutorDTO("Mario Rossi", null, "055 123456")))
                .isEqualTo("Mario Rossi (055 123456)");
        assertThat(CompanyTutorLine.of(new CompanyTutorDTO("Mario Rossi", "Capo reparto", null)))
                .isEqualTo("Mario Rossi, Capo reparto");
        assertThat(CompanyTutorLine.of(new CompanyTutorDTO("Mario Rossi", null, null))).isEqualTo("Mario Rossi");
    }
}
