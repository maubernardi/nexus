package it.nexus.domain;

import static it.nexus.domain.TestEntities.company;
import static it.nexus.domain.TestEntities.jobCategory;
import static it.nexus.domain.TestEntities.jobSlot;
import static it.nexus.domain.TestEntities.zone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import it.nexus.TestcontainersConfiguration;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ZoneRepository;

/**
 * Modifiche concorrenti (spec domain-model, "Due operatori sulla stessa mansione"): transazioni reali e separate,
 * quindi niente @Transactional sul test; i dati creati vengono rimossi alla fine.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class OptimisticLockingIT {

    @Autowired TransactionTemplate tx;
    @Autowired JobSlotRepository jobSlots;
    @Autowired CompanyRepository companies;
    @Autowired JobCategoryRepository categories;
    @Autowired ZoneRepository zones;

    private Long slotId;
    private Long companyId;
    private Long categoryId;
    private Long zoneId;

    @AfterEach
    void cleanUp() {
        tx.executeWithoutResult(status -> {
            if (slotId != null) {
                jobSlots.deleteById(slotId);
            }
            companies.deleteById(companyId);
            categories.deleteById(categoryId);
            zones.deleteById(zoneId);
        });
    }

    @Test
    void secondaModificaDallaStessaVersione_vieneRifiutata() {
        slotId = tx.execute(status -> {
            Company company = companies.save(company(DomainFixtures.vatCode()));
            JobCategory category = categories.save(jobCategory(DomainFixtures.code("C")));
            Zone zone = zones.save(zone(DomainFixtures.code("Z")));
            companyId = company.getId();
            categoryId = category.getId();
            zoneId = zone.getId();
            return jobSlots.save(jobSlot(company, category, zone)).getId();
        });

        // due operatori leggono la stessa versione della mansione
        JobSlot operatoreA = tx.execute(status -> jobSlots.findById(slotId).orElseThrow());
        JobSlot operatoreB = tx.execute(status -> jobSlots.findById(slotId).orElseThrow());
        assertThat(operatoreA.getVersion()).isEqualTo(operatoreB.getVersion());

        // il primo salva
        operatoreA.setTitle("Modificata da A");
        tx.executeWithoutResult(status -> jobSlots.save(operatoreA));

        // il secondo parte dalla versione ormai superata: rifiutato, niente sovrascrittura
        operatoreB.setTitle("Modificata da B");
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> jobSlots.save(operatoreB)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        String titoloFinale = tx.execute(status -> jobSlots.findById(slotId).orElseThrow().getTitle());
        assertThat(titoloFinale).isEqualTo("Modificata da A");
    }
}
