package it.nexus.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.domain.Company;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.MunicipalityImportRepository;
import it.nexus.repository.MunicipalityRepository;
import it.nexus.repository.ProvinceRepository;
import it.nexus.services.MunicipalityService;
import jakarta.persistence.EntityManager;

/**
 * La demo carica da sola il file ISTAT vero (US-114): tutto l'elenco, una volta sola; poi dà il comune alle aziende
 * dimostrative che non lo hanno (US-1007).
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class MunicipalitySeedIT {

    @Autowired MunicipalityImportRepository imports;
    @Autowired MunicipalityService municipalityService;
    @Autowired MunicipalityRepository municipalities;
    @Autowired ProvinceRepository provinces;
    @Autowired CompanyRepository companies;
    @Autowired DataSource dataSource;
    @Autowired EntityManager em;

    @Test
    void caricaLElencoVeroUnaSolaVolta() throws Exception {
        MunicipalitySeed seed = new MunicipalitySeed(imports, municipalityService, dataSource);
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

    @Test
    void comuneDelleAziendeDimostrative() throws Exception {
        MunicipalitySeed seed = new MunicipalitySeed(imports, municipalityService, dataSource);
        seed.run(null);
        Company demo = new Company("Demo Logistica S.r.l.", "99000000001");
        demo.setLegalAddress("Via dell'Esempio 1, Città Demo");
        Company corrected = new Company("Demo Ristorazione S.n.c.", "99000000002");
        corrected.setLegalAddress("Piazza della Signoria 1");
        corrected.setLegalMunicipality(municipalities.findByValidToIsNull().stream()
                .filter(m -> m.getName().equals("Prato")).findFirst().orElseThrow());
        Company other = new Company("Azienda vera", "01234567890");
        other.setLegalAddress("Via Roma 1, Città Demo");
        companies.saveAll(List.of(demo, corrected, other));
        em.flush();

        seed.run(null);
        seed.run(null);
        em.clear();

        Company seeded = companies.findByVatCode("99000000001").orElseThrow();
        assertThat(seeded.getLegalAddress()).isEqualTo("Via dell'Esempio 1");
        assertThat(seeded.getLegalMunicipality().getName()).isEqualTo("Sesto Fiorentino");
        assertThat(companies.findByVatCode("99000000002").orElseThrow().getLegalMunicipality().getName())
                .isEqualTo("Prato");
        Company untouched = companies.findByVatCode("01234567890").orElseThrow();
        assertThat(untouched.getLegalMunicipality()).isNull();
        assertThat(untouched.getLegalAddress()).isEqualTo("Via Roma 1, Città Demo");
    }
}
