package it.nexus.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import it.nexus.TestcontainersConfiguration;
import it.nexus.repository.ZoneRepository;

/** Le zone reali di partenza (V11) ci sono in ogni ambiente, anche senza dati demo. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class InitialZonesIT {

    @Autowired ZoneRepository zones;

    @Test
    void quartieriDiFirenzeEComuniDellHinterland() {
        assertThat(zones.findAll())
                .filteredOn(z -> "system".equals(z.getCreatedBy()))
                .hasSize(40)
                .allMatch(Zone::isActive)
                .extracting(Zone::getName)
                // quartieri con il prefisso "Firenze " (V14), comuni dell'hinterland senza
                .contains("Firenze Novoli", "Firenze San Niccolò", "Firenze Porta al Prato", "Firenze Casellina",
                        "Scarperia e San Piero", "Fiesole")
                .doesNotContain("Novoli", "Firenze Fiesole");
        assertThat(zones.findByCode("CAMPO_DI_MARTE")).get().extracting(Zone::getName).isEqualTo("Firenze Campo di Marte");
        assertThat(zones.findByCode("SESTO_FIORENTINO")).get().extracting(Zone::getName).isEqualTo("Sesto Fiorentino");
    }
}
