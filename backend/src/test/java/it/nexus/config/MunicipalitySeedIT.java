package it.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.repository.MunicipalityImportRepository;
import it.nexus.repository.MunicipalityRepository;
import it.nexus.repository.ProvinceRepository;
import it.nexus.services.MunicipalityService;

/** La demo carica da sola il file ISTAT vero (US-114): tutto l'elenco, una volta sola. */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class MunicipalitySeedIT {

    @Autowired MunicipalityImportRepository imports;
    @Autowired MunicipalityService municipalityService;
    @Autowired MunicipalityRepository municipalities;
    @Autowired ProvinceRepository provinces;

    @Test
    void caricaLElencoVeroUnaSolaVolta() throws Exception {
        MunicipalitySeed seed = new MunicipalitySeed(imports, municipalityService);
        seed.run(null);
        long loaded = municipalities.countByValidToIsNull();
        assertThat(loaded).isGreaterThan(7800);
        assertThat(provinces.countByValidToIsNull()).isGreaterThan(100);
        assertThat(municipalityService.search("sesto fio", false))
                .singleElement()
                .satisfies(m -> assertThat(m.provinceAbbreviation()).isEqualTo("FI"));

        seed.run(null);
        assertThat(municipalities.count()).isEqualTo(loaded);
    }
}
