package it.nexus.support;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.test.context.TestComponent;

import it.nexus.domain.AppUser;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.BoardPost;
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.UserProject;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.enumeration.TicketType;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;

/**
 * Preparazione comune dei test d'integrazione del flusso (azione A7 della retrospettiva): utenti della sicurezza mock,
 * progetti con i loro membri, mansioni libere, annunci pubblicati e segnalazioni in un certo stato, senza ripetere in
 * ogni test le stesse righe. Codici univoci: i test girano in transazioni che si annullano, ma nello stesso database.
 */
@TestComponent
@RequiredArgsConstructor
public class WorkflowScenarios {

    private static final AtomicLong SEQ = new AtomicLong(5_000);

    private final AppUserRepository users;
    private final ProjectRepository projects;
    private final UserProjectRepository userProjects;
    private final ZoneRepository zones;
    private final JobCategoryRepository categories;
    private final CompanyRepository companies;
    private final JobSlotRepository jobSlots;
    private final BeneficiaryRepository beneficiaries;
    private final BoardPostRepository boardPosts;
    private final TicketRepository tickets;

    /** Gli utenti della sicurezza mock (header {@code X-USER-ID}), con gli stessi id esterni della configurazione. */
    public record Actors(AppUser tutor1, AppUser tutor2, AppUser operator, AppUser admin) {
    }

    /** Dove si svolge la scena: una zona, una tipologia e un'azienda attiva. */
    public record Place(Zone zone, JobCategory category, Company company) {
    }

    public Actors actors() {
        return new Actors(
                users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001")),
                users.save(TestEntities.user("tutor2", Role.TUTOR, "00000000-0000-4000-8000-000000000002")),
                users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER, "00000000-0000-4000-8000-000000000003")),
                users.save(TestEntities.user("admin", Role.ADMIN, "00000000-0000-4000-8000-000000000004")));
    }

    /** Progetto attivo con i Tutor indicati come membri. */
    public Project project(String prefix, AppUser... members) {
        Project project = projects.save(TestEntities.project(code(prefix)));
        for (AppUser member : members) {
            userProjects.save(new UserProject(member, project));
        }
        return project;
    }

    public Place place() {
        Zone zone = zones.save(TestEntities.zone(code("Z")));
        JobCategory category = categories.save(TestEntities.jobCategory(code("C")));
        Company company = TestEntities.company(String.format("%011d", 20_000_000_000L + SEQ.incrementAndGet()));
        return new Place(zone, category, companies.save(company));
    }

    public JobSlot freeSlot(Place place, String title) {
        JobSlot slot = TestEntities.jobSlot(place.company(), place.category(), place.zone());
        slot.setTitle(title);
        return jobSlots.save(slot);
    }

    /** Annuncio pubblicato ora sulla mansione; {@code project} nullo = aperto a tutti. */
    public BoardPost publishedPost(JobSlot slot, Project project) {
        BoardPost post = TestEntities.boardPost(slot);
        post.setTitle("Annuncio " + slot.getTitle());
        post.setProject(project);
        post.setStatus(BoardPostStatus.PUBLISHED);
        post.setPublishedAt(Instant.now());
        return boardPosts.saveAndFlush(post);
    }

    /** Un nuovo beneficiario del Tutor, senza segnalazioni. */
    public Beneficiary beneficiary(AppUser tutor, Place place) {
        return beneficiaries.save(TestEntities.beneficiary(tutor, place.zone()));
    }

    /**
     * Segnalazione speciale candidata all'annuncio, in lavorazione con la corsia veloce (come la crea US-306);
     * {@code operator} non nullo = già presa in carico.
     */
    public Ticket specialTicket(AppUser tutor, Project project, BoardPost post, AppUser operator) {
        Ticket ticket = TestEntities.ticket(tutor, project, beneficiary(tutor, placeOf(post)),
                post.getJobSlot().getJobCategory());
        ticket.setType(TicketType.SPECIAL);
        ticket.setFastTrack(true);
        ticket.setBoardPost(post);
        ticket.setStatus(TicketStatus.IN_LAVORAZIONE);
        ticket.setAssignedCcOperator(operator);
        return tickets.saveAndFlush(ticket);
    }

    /**
     * Segnalazione con l'appuntamento fissato (US-801) sulla mansione, che resta bloccata dalla segnalazione; in carico
     * all'operatore indicato.
     */
    public Ticket appointmentTicket(AppUser tutor, Project project, JobSlot slot, AppUser operator) {
        Ticket ticket = TestEntities.ticket(tutor, project, beneficiary(tutor, placeOf(slot)), slot.getJobCategory());
        ticket.setStatus(TicketStatus.APPUNTAMENTO);
        ticket.setAssignedCcOperator(operator);
        ticket.setJobSlot(slot);
        Ticket saved = tickets.saveAndFlush(ticket);
        slot.blockFor(saved);
        jobSlots.saveAndFlush(slot);
        return saved;
    }

    private static Place placeOf(BoardPost post) {
        return placeOf(post.getJobSlot());
    }

    private static Place placeOf(JobSlot slot) {
        return new Place(slot.getZone(), slot.getJobCategory(), slot.getCompany());
    }

    private static String code(String prefix) {
        return prefix + "_" + SEQ.incrementAndGet();
    }
}
