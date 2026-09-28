package it.nexus.domain;

import static it.nexus.domain.TestEntities.boardPost;
import static it.nexus.domain.TestEntities.company;
import static it.nexus.domain.TestEntities.project;
import static it.nexus.domain.TestEntities.ticket;
import static it.nexus.domain.TestEntities.user;
import static it.nexus.domain.TestEntities.zone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.domain.DomainFixtures.Graph;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.domain.enumeration.LanguageLevel;
import it.nexus.domain.enumeration.LicenseType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketType;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.CandidateRepository;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketCompanyBlacklistRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.repository.ZoneRepository;
import jakarta.persistence.EntityManager;

/**
 * Vincoli di integrità garantiti dal database (spec domain-model e company-audit-trail), verificati su PostgreSQL
 * reale. Ogni test controlla che scatti il vincolo atteso, per nome. I casi non esprimibili tramite le entità
 * (valori fuori dagli enum, modifiche all'audit trail) passano da SQL diretto.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, DomainFixtures.class})
class DomainConstraintsIT {

    @Autowired DomainFixtures fixtures;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired ProjectRepository projects;
    @Autowired ZoneRepository zones;
    @Autowired AppUserRepository users;
    @Autowired UserProjectRepository userProjects;
    @Autowired CompanyRepository companies;
    @Autowired JobSlotRepository jobSlots;
    @Autowired CandidateRepository candidates;
    @Autowired TicketRepository tickets;
    @Autowired TicketCompanyBlacklistRepository blacklist;
    @Autowired BoardPostRepository boardPosts;
    @Autowired CompanyAuditEventRepository auditEvents;

    /** L'operazione deve fallire per il vincolo indicato (nome del vincolo o testo dell'errore del database). */
    static void assertViolates(String constraint, ThrowingCallable operation) {
        assertThatThrownBy(operation)
                .isInstanceOf(DataAccessException.class)
                .rootCause().hasMessageContaining(constraint);
    }

    @Nested
    class Unicita {

        @Test
        void codiceProgettoDuplicato() {
            String code = DomainFixtures.code("GOL");
            projects.saveAndFlush(project(code));
            assertViolates("uq_project_code", () -> projects.saveAndFlush(project(code)));
        }

        @Test
        void codiceZonaDuplicato() {
            String code = DomainFixtures.code("NORD");
            zones.saveAndFlush(zone(code));
            assertViolates("uq_zone_code", () -> zones.saveAndFlush(zone(code)));
        }

        @Test
        void partitaIvaDuplicata() {
            String vat = DomainFixtures.vatCode();
            companies.saveAndFlush(company(vat));
            assertViolates("uq_company_vat_code", () -> companies.saveAndFlush(company(vat)));
        }

        @Test
        void partitaIvaNonValida() {
            assertViolates("ck_company_vat_code", () -> companies.saveAndFlush(company("12AB")));
        }

        @Test
        void externalIdDuplicato() {
            users.saveAndFlush(user("utente.a", Role.TUTOR, "sub-dup"));
            assertViolates("uq_app_user_external_id", () -> users.saveAndFlush(user("utente.b", Role.TUTOR, "sub-dup")));
        }

        @Test
        void emailDuplicataSenzaDistinzioneMaiuscole() {
            users.saveAndFlush(user("mario", Role.TUTOR));
            AppUser other = user("mario2", Role.TUTOR);
            other.setEmail("MARIO@nexus.test");
            assertViolates("uq_app_user_email_lower", () -> users.saveAndFlush(other));
        }

        @Test
        void assegnazioneProgettoDuplicata() {
            Graph g = fixtures.graph();
            userProjects.saveAndFlush(new UserProject(g.tutor(), g.project()));
            em.clear();
            assertViolates("pk_user_project", () -> userProjects.saveAndFlush(new UserProject(g.tutor(), g.project())));
        }

        @Test
        void aziendaEsclusaDueVolte() {
            Graph g = fixtures.graph();
            Ticket t = fixtures.ticket(g);
            blacklist.saveAndFlush(new TicketCompanyBlacklist(t, g.company(), "prima esclusione"));
            em.clear();
            assertViolates("pk_ticket_company_blacklist",
                    () -> blacklist.saveAndFlush(new TicketCompanyBlacklist(t, g.company(), "seconda esclusione")));
        }
    }

    @Nested
    class Numerazione {

        @Test
        void ticketEPostHannoSequenzeIndipendenti() {
            Graph g = fixtures.graph();
            Ticket t1 = fixtures.ticket(g);
            Ticket t2 = fixtures.ticket(g);
            BoardPost p1 = boardPosts.saveAndFlush(boardPost(g.slot()));
            BoardPost p2 = boardPosts.saveAndFlush(boardPost(fixtures.slot(g)));

            assertThat(t2.getNumber()).isGreaterThan(t1.getNumber());
            assertThat(p2.getNumber()).isGreaterThan(p1.getNumber());
            // la sequenza dei post non consuma numeri dei ticket
            assertThat(fixtures.ticket(g).getNumber()).isEqualTo(t2.getNumber() + 1);
        }
    }

    @Nested
    class TicketVincoli {

        @Test
        void mansioneRichiestaDoppia() {
            Graph g = fixtures.graph();
            Ticket t = ticket(g.tutor(), g.project(), g.candidate(), g.category());
            t.setRequestedJobFreeText("Anche testo libero");
            assertViolates("ck_ticket_requested_job", () -> tickets.saveAndFlush(t));
        }

        @Test
        void mansioneRichiestaAssente() {
            Graph g = fixtures.graph();
            Ticket t = ticket(g.tutor(), g.project(), g.candidate(), null);
            assertViolates("ck_ticket_requested_job", () -> tickets.saveAndFlush(t));
        }

        @Test
        void testoLiberoVuoto() {
            Graph g = fixtures.graph();
            Ticket t = ticket(g.tutor(), g.project(), g.candidate(), null);
            t.setRequestedJobFreeText("   ");
            assertViolates("ck_ticket_requested_job_text", () -> tickets.saveAndFlush(t));
        }

        @Test
        void segnalazioneSpecialeSenzaPost() {
            Graph g = fixtures.graph();
            Ticket t = ticket(g.tutor(), g.project(), g.candidate(), g.category());
            t.setType(TicketType.SPECIAL);
            assertViolates("ck_ticket_special_board_post", () -> tickets.saveAndFlush(t));
        }

        @Test
        void segnalazioneSpecialeConPostEAmmessa() {
            Graph g = fixtures.graph();
            BoardPost post = boardPosts.saveAndFlush(boardPost(g.slot()));
            Ticket t = ticket(g.tutor(), g.project(), g.candidate(), g.category());
            t.setType(TicketType.SPECIAL);
            t.setFastTrack(true);
            t.setBoardPost(post);
            assertThat(tickets.saveAndFlush(t).getNumber()).isNotNull();
        }

        @Test
        void fastTrackSoloPerSpeciali() {
            Graph g = fixtures.graph();
            Ticket t = ticket(g.tutor(), g.project(), g.candidate(), g.category());
            t.setFastTrack(true);
            assertViolates("ck_ticket_fast_track", () -> tickets.saveAndFlush(t));
        }

        @Test
        void timerAvviatoSenzaScadenza() {
            Graph g = fixtures.graph();
            Ticket t = fixtures.ticket(g);
            t.setTimerStartedAt(Instant.now());
            assertViolates("ck_ticket_timer", () -> tickets.saveAndFlush(t));
        }

        @Test
        void scadenzaPrecedenteAllAvvio() {
            Graph g = fixtures.graph();
            Ticket t = fixtures.ticket(g);
            t.setTimerStartedAt(Instant.now());
            t.setTimerDeadlineAt(Instant.now().minus(1, ChronoUnit.DAYS));
            assertViolates("ck_ticket_timer_deadline", () -> tickets.saveAndFlush(t));
        }

        @Test
        void promemoriaSenzaTimer() {
            Graph g = fixtures.graph();
            Ticket t = fixtures.ticket(g);
            t.setTimerReminderSentAt(Instant.now());
            assertViolates("ck_ticket_timer_reminder", () -> tickets.saveAndFlush(t));
        }

        @Test
        void statoNonPrevisto() {
            Graph g = fixtures.graph();
            Ticket t = fixtures.ticket(g);
            assertViolates("ck_ticket_status",
                    () -> jdbc.update("UPDATE ticket SET status = 'CHIUSA' WHERE id = ?", t.getId()));
        }

        @Test
        void tutorConTicketNonCancellabile() {
            Graph g = fixtures.graph();
            fixtures.ticket(g);
            // SQL diretto: Hibernate fermerebbe prima la cancellazione; qui si verifica il vincolo del database
            assertViolates("violates foreign key constraint",
                    () -> jdbc.update("DELETE FROM app_user WHERE id = ?", g.tutor().getId()));
        }
    }

    @Nested
    class Mansione {

        @Test
        void bloccataSenzaTicket() {
            Graph g = fixtures.graph();
            g.slot().setStatus(JobSlotStatus.BLOCCATA);
            assertViolates("ck_job_slot_blocked", () -> jobSlots.saveAndFlush(g.slot()));
        }

        @Test
        void ticketSuMansioneLibera() {
            Graph g = fixtures.graph();
            Ticket t = fixtures.ticket(g);
            g.slot().setBlockedByTicket(t);
            assertViolates("ck_job_slot_blocked", () -> jobSlots.saveAndFlush(g.slot()));
        }

        @Test
        void unTicketBloccaAlMassimoUnaMansione() {
            Graph g = fixtures.graph();
            Ticket t = fixtures.ticket(g);
            g.slot().blockFor(t);
            jobSlots.saveAndFlush(g.slot());
            JobSlot second = fixtures.slot(g);
            second.blockFor(t);
            assertViolates("uq_job_slot_blocked_by_ticket", () -> jobSlots.saveAndFlush(second));
        }

        @Test
        void bloccoESblocco() {
            Graph g = fixtures.graph();
            Ticket t = fixtures.ticket(g);
            g.slot().blockFor(t);
            jobSlots.saveAndFlush(g.slot());
            g.slot().release();
            jobSlots.saveAndFlush(g.slot());
            assertThat(jobSlots.findByBlockedByTicketId(t.getId())).isEmpty();
        }
    }

    @Nested
    class Bacheca {

        @Test
        void unSoloPostAttivoPerMansione() {
            Graph g = fixtures.graph();
            BoardPost published = boardPost(g.slot());
            published.setStatus(BoardPostStatus.PUBLISHED);
            published.setPublishedAt(Instant.now());
            boardPosts.saveAndFlush(published);
            assertViolates("uq_board_post_active_job_slot", () -> boardPosts.saveAndFlush(boardPost(g.slot())));
        }

        @Test
        void dopoLArchiviazioneSiPuoRipubblicare() {
            Graph g = fixtures.graph();
            BoardPost first = boardPosts.saveAndFlush(boardPost(g.slot()));
            first.setStatus(BoardPostStatus.ARCHIVED);
            boardPosts.saveAndFlush(first);
            assertThat(boardPosts.saveAndFlush(boardPost(g.slot())).getNumber()).isGreaterThan(first.getNumber());
        }

        @Test
        void pubblicatoSenzaData() {
            Graph g = fixtures.graph();
            BoardPost post = boardPost(g.slot());
            post.setStatus(BoardPostStatus.PUBLISHED);
            assertViolates("ck_board_post_published", () -> boardPosts.saveAndFlush(post));
        }

        @Test
        void resoPubblicoConProgetto() {
            Graph g = fixtures.graph();
            BoardPost post = boardPost(g.slot());
            post.setProject(g.project());
            post.setMadePublicAt(Instant.now());
            assertViolates("ck_board_post_made_public", () -> boardPosts.saveAndFlush(post));
        }

        @Test
        void etaMinimaSuperioreAllaMassima() {
            Graph g = fixtures.graph();
            BoardPost post = boardPost(g.slot());
            post.setAgeMin(30);
            post.setAgeMax(18);
            assertViolates("ck_board_post_age_range", () -> boardPosts.saveAndFlush(post));
        }

        @Test
        void oreSettimanaliNonPositive() {
            Graph g = fixtures.graph();
            BoardPost post = boardPost(g.slot());
            post.setWeeklyHours(0);
            assertViolates("ck_board_post_weekly_hours", () -> boardPosts.saveAndFlush(post));
        }

        @Test
        void durataNonPositiva() {
            Graph g = fixtures.graph();
            BoardPost post = boardPost(g.slot());
            post.setDurationMonths(0);
            assertViolates("ck_board_post_duration", () -> boardPosts.saveAndFlush(post));
        }
    }

    @Nested
    class Candidato {

        @Test
        void patenteDichiarataSenzaTipi() {
            Graph g = fixtures.graph();
            g.candidate().setHasDrivingLicense(true);
            assertViolates("ck_candidate_driving_license", () -> candidates.saveAndFlush(g.candidate()));
        }

        @Test
        void tipiDiPatenteCoerenti() {
            Graph g = fixtures.graph();
            g.candidate().setLicenseTypes(LicenseType.B, LicenseType.CQC);
            candidates.saveAndFlush(g.candidate());
            assertThat(g.candidate().isHasDrivingLicense()).isTrue();
        }

        @Test
        void tipoDiPatenteEstraneo() {
            Graph g = fixtures.graph();
            assertViolates("ck_candidate_license_types", () -> jdbc.update(
                    "UPDATE candidate SET license_types = '{B,Z9}', has_driving_license = true WHERE id = ?",
                    g.candidate().getId()));
        }

        @Test
        void nazionalitaNonIso() {
            Graph g = fixtures.graph();
            g.candidate().setNationality("it");
            assertViolates("ck_candidate_nationality", () -> candidates.saveAndFlush(g.candidate()));
        }

        @Test
        void nomeObbligatorioSeNonAnonimizzato() {
            Graph g = fixtures.graph();
            g.candidate().setFirstName(null);
            assertViolates("ck_candidate_name", () -> candidates.saveAndFlush(g.candidate()));
        }

        @Test
        void anonimizzatoSenzaNomeEAmmesso() {
            Graph g = fixtures.graph();
            g.candidate().setFirstName(null);
            g.candidate().setLastName(null);
            g.candidate().setAnonymizedAt(Instant.now());
            candidates.saveAndFlush(g.candidate());
            assertThat(g.candidate().getAnonymizedAt()).isNotNull();
        }

        @Test
        void livelloLinguisticoNonValido() {
            Graph g = fixtures.graph();
            assertViolates("ck_candidate_language_level", () -> jdbc.update(
                    "INSERT INTO candidate_language (id, candidate_id, language, level, created_at, created_by, updated_at, updated_by)"
                            + " VALUES (1, ?, 'it', 'B3', now(), 't', now(), 't')",
                    g.candidate().getId()));
        }

        @Test
        void linguaDuplicata() {
            Graph g = fixtures.graph();
            g.candidate().addLanguage(new CandidateLanguage("it", LanguageLevel.C2));
            g.candidate().addLanguage(new CandidateLanguage("it", LanguageLevel.B1));
            assertViolates("uq_candidate_language", () -> candidates.saveAndFlush(g.candidate()));
        }
    }

    @Nested
    class AuditTrail {

        private Long registerEvent() {
            Graph g = fixtures.graph();
            CompanyAuditEvent event = auditEvents.save(
                    new CompanyAuditEvent(g.company(), null, "TIROCINIO_CONCLUSO", Map.of("assunto", true)));
            em.flush();
            return event.getId();
        }

        @Test
        void eventoRegistratoEConsultabile() {
            Long id = registerEvent();
            em.clear();
            assertThat(auditEvents.findById(id)).get()
                    .extracting(CompanyAuditEvent::getEventType).isEqualTo("TIROCINIO_CONCLUSO");
        }

        @Test
        void modificaRifiutata() {
            Long id = registerEvent();
            assertViolates("company_audit_event è in sola aggiunta",
                    () -> jdbc.update("UPDATE company_audit_event SET event_type = 'MANOMESSO' WHERE id = ?", id));
        }

        @Test
        void cancellazioneRifiutata() {
            Long id = registerEvent();
            assertViolates("company_audit_event è in sola aggiunta",
                    () -> jdbc.update("DELETE FROM company_audit_event WHERE id = ?", id));
        }

        @Test
        void svuotamentoRifiutato() {
            registerEvent();
            assertViolates("company_audit_event è in sola aggiunta",
                    () -> jdbc.execute("TRUNCATE company_audit_event"));
        }
    }
}
