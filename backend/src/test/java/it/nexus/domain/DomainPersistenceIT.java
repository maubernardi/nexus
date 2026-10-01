package it.nexus.domain;

import static it.nexus.domain.TestEntities.boardPost;
import static it.nexus.domain.TestEntities.beneficiary;
import static it.nexus.domain.TestEntities.company;
import static it.nexus.domain.TestEntities.jobCategory;
import static it.nexus.domain.TestEntities.jobSlot;
import static it.nexus.domain.TestEntities.project;
import static it.nexus.domain.TestEntities.ticket;
import static it.nexus.domain.TestEntities.user;
import static it.nexus.domain.TestEntities.zone;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import io.hypersistence.tsid.TSID;
import it.nexus.TestcontainersConfiguration;
import it.nexus.config.PersistenceConfig;
import it.nexus.config.security.AuthenticatedUser;
import it.nexus.config.security.UserAuthenticationToken;
import it.nexus.domain.enumeration.LanguageLevel;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

/** Verifica la mappatura entità ↔ schema su PostgreSQL reale: id, audit, numeri progressivi, array, JSON. */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class DomainPersistenceIT {

    @Autowired EntityManager em;
    @Autowired ProjectRepository projects;
    @Autowired ZoneRepository zones;
    @Autowired JobCategoryRepository categories;
    @Autowired AppUserRepository users;
    @Autowired UserProjectRepository userProjects;
    @Autowired CompanyRepository companies;
    @Autowired JobSlotRepository jobSlots;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired BoardPostRepository boardPosts;
    @Autowired CompanyAuditEventRepository auditEvents;

    @BeforeEach
    void authenticate() {
        AuthenticatedUser operator = new AuthenticatedUser("ext-op", "operatore.cc", "Op", "CC", "op@nexus.test",
                Set.of(Role.CALL_CENTER));
        SecurityContextHolder.getContext().setAuthentication(new UserAuthenticationToken(operator, null));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void persist_assignsTsidAndAuditColumns() {
        Project project = projects.saveAndFlush(project("GOL"));

        assertThat(project.getId()).isNotNull().isPositive();
        assertThat(TSID.from(project.getId()).toString()).hasSize(13);
        assertThat(project.getCreatedBy()).isEqualTo("operatore.cc");
        assertThat(project.getUpdatedBy()).isEqualTo("operatore.cc");
        assertThat(project.getCreatedAt()).isNotNull();
        assertThat(projects.findByCode("GOL")).contains(project);
    }

    @Test
    void persist_usesSystemAuditor_withoutAuthenticatedUser() {
        SecurityContextHolder.clearContext();

        Zone zone = zones.saveAndFlush(zone("NORD"));

        assertThat(zone.getCreatedBy()).isEqualTo(PersistenceConfig.SYSTEM_AUDITOR);
    }

    @Test
    void ticketAndBoardPost_receiveProgressiveNumbersFromDatabase() {
        Fixture f = fixture();

        Ticket first = tickets.saveAndFlush(ticket(f.tutor, f.project, f.beneficiary, f.category));
        // un altro beneficiario: ognuno ha al più una segnalazione aperta
        Ticket second = tickets.saveAndFlush(ticket(f.tutor, f.project, beneficiaries.save(beneficiary(f.tutor, f.zone)),
                f.category));
        BoardPost post = boardPosts.saveAndFlush(boardPost(f.slot));

        assertThat(first.getNumber()).isNotNull();
        assertThat(second.getNumber()).isGreaterThan(first.getNumber());
        assertThat(post.getNumber()).isNotNull();
        assertThat(tickets.findByNumber(second.getNumber())).contains(second);
    }

    @Test
    void beneficiary_roundTripsLicenseArrayAndLanguages() {
        Fixture f = fixture();
        Beneficiary beneficiary = beneficiary(f.tutor, f.zone);
        beneficiary.setLicenseTypes(LicenseType.B, LicenseType.CQC);
        beneficiary.addLanguage(new BeneficiaryLanguage("it", LanguageLevel.MADRELINGUA));
        beneficiary.addLanguage(new BeneficiaryLanguage("en", LanguageLevel.B1));
        Long id = beneficiaries.saveAndFlush(beneficiary).getId();
        em.clear();

        Beneficiary reloaded = beneficiaries.findById(id).orElseThrow();

        assertThat(reloaded.getLicenseTypes()).containsExactly(LicenseType.B, LicenseType.CQC);
        assertThat(reloaded.isHasDrivingLicense()).isTrue();
        assertThat(reloaded.getLanguages()).extracting(BeneficiaryLanguage::getLanguage).containsExactlyInAnyOrder("it", "en");
    }

    @Test
    void jobSlot_blockAndRelease_incrementVersion() {
        Fixture f = fixture();
        Ticket ticket = tickets.saveAndFlush(ticket(f.tutor, f.project, f.beneficiary, f.category));
        long initialVersion = f.slot.getVersion();

        f.slot.blockFor(ticket);
        jobSlots.saveAndFlush(f.slot);

        assertThat(f.slot.getVersion()).isGreaterThan(initialVersion);
        assertThat(jobSlots.findByBlockedByTicketId(ticket.getId())).contains(f.slot);
    }

    @Test
    void userProject_andAuditEvent_arePersisted() {
        Fixture f = fixture();
        userProjects.saveAndFlush(new UserProject(f.tutor, f.project));
        auditEvents.save(new CompanyAuditEvent(f.company, null, "PROPOSTA_INVIATA", Map.of("note", "prova", "ore", 20)));
        em.flush();
        em.clear();

        assertThat(userProjects.findByIdUserId(f.tutor.getId())).hasSize(1);
        CompanyAuditEvent event = auditEvents.findByCompanyIdOrderByCreatedAtAsc(f.company.getId()).getFirst();
        assertThat(event.getDetails()).containsEntry("note", "prova").containsEntry("ore", 20);
        assertThat(event.getCreatedBy()).isEqualTo("operatore.cc");
    }

    @Test
    void seedData_isNotLoaded_inTestProfile() {
        // i dati dimostrativi (db/seed) esistono solo nel profilo local: qui il database parte vuoto
        assertThat(users.findByUsername("admin")).isEmpty();
        assertThat(zones.findByCode("ZONA_NORD")).isEmpty();
        assertThat(tickets.count()).isZero();
    }

    @Test
    void toString_doesNotExposePersonalData() {
        Fixture f = fixture();

        assertThat(f.beneficiary.toString()).doesNotContain("Mario", "Rossi").matches("Beneficiary\\[id=[0-9A-Z]{13}]");
        assertThat(f.tutor.toString()).doesNotContain("tutor-it@", "Nome");
    }

    private Fixture fixture() {
        Fixture f = new Fixture();
        f.project = projects.save(project("P" + System.nanoTime() % 100000));
        f.zone = zones.save(zone("Z" + System.nanoTime() % 100000));
        f.category = categories.save(jobCategory("C" + System.nanoTime() % 100000));
        f.tutor = users.save(user("tutor-it-" + System.nanoTime(), Role.TUTOR));
        f.company = companies.save(company(String.format("%011d", System.nanoTime() % 100_000_000_000L)));
        f.slot = jobSlots.save(jobSlot(f.company, f.category, f.zone));
        f.beneficiary = beneficiaries.save(beneficiary(f.tutor, f.zone));
        em.flush();
        return f;
    }

    private static final class Fixture {
        Project project;
        Zone zone;
        JobCategory category;
        AppUser tutor;
        Company company;
        JobSlot slot;
        Beneficiary beneficiary;
    }
}
