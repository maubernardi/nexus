package it.nexus.domain;

import static it.nexus.domain.TestEntities.candidate;
import static it.nexus.domain.TestEntities.company;
import static it.nexus.domain.TestEntities.jobCategory;
import static it.nexus.domain.TestEntities.jobSlot;
import static it.nexus.domain.TestEntities.project;
import static it.nexus.domain.TestEntities.user;
import static it.nexus.domain.TestEntities.zone;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.test.context.TestComponent;

import it.nexus.domain.enumeration.Role;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.CandidateRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/** Crea grafi di entità valide con codici univoci, per i test d'integrazione dei vincoli. */
@TestComponent
@RequiredArgsConstructor
public class DomainFixtures {

    private static final AtomicLong SEQ = new AtomicLong(1_000);

    private final EntityManager em;
    private final ProjectRepository projects;
    private final ZoneRepository zones;
    private final JobCategoryRepository categories;
    private final AppUserRepository users;
    private final CompanyRepository companies;
    private final JobSlotRepository jobSlots;
    private final CandidateRepository candidates;
    private final TicketRepository tickets;

    public record Graph(Project project, Zone zone, JobCategory category, AppUser tutor, Company company, JobSlot slot,
            Candidate candidate) {
    }

    public static String code(String prefix) {
        return prefix + "_" + SEQ.incrementAndGet();
    }

    public static String vatCode() {
        return String.format("%011d", 10_000_000_000L + SEQ.incrementAndGet());
    }

    /** Progetto, zona, tipologia, tutor, azienda con una mansione libera e un candidato, già salvati. */
    public Graph graph() {
        Project project = projects.save(project(code("P")));
        Zone zone = zones.save(zone(code("Z")));
        JobCategory category = categories.save(jobCategory(code("C")));
        AppUser tutor = users.save(user(code("tutor").toLowerCase(), Role.TUTOR));
        Company company = companies.save(company(vatCode()));
        JobSlot slot = jobSlots.save(jobSlot(company, category, zone));
        Candidate candidate = candidates.save(candidate(tutor, zone));
        em.flush();
        return new Graph(project, zone, category, tutor, company, slot, candidate);
    }

    public JobSlot slot(Graph g) {
        return jobSlots.saveAndFlush(jobSlot(g.company(), g.category(), g.zone()));
    }

    /** Ticket NORMAL in stato NUOVA con mansione richiesta dal catalogo, già salvato. */
    public Ticket ticket(Graph g) {
        return tickets.saveAndFlush(TestEntities.ticket(g.tutor(), g.project(), g.candidate(), g.category()));
    }
}
