package it.nexus.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.TimeConfig;
import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.JobCategory;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.Zone;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.repository.AppUserRepository;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.JobCategoryRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.NotificationRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.ZoneRepository;

/**
 * Promemoria del timer (US-704), chiamato come fa l'attività oraria: senza utente collegato e con istanti precisi.
 * Accettazione lunedì 2 novembre 2026: il 4° giorno lavorativo è venerdì 6 novembre.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TimerServiceIT {

    private static final Instant ACCEPTED = at(2026, 11, 2, 9, 0);

    @Autowired TimerService timerService;
    @Autowired AppUserRepository users;
    @Autowired ProjectRepository projects;
    @Autowired ZoneRepository zones;
    @Autowired JobCategoryRepository categories;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired NotificationRepository notifications;
    @Autowired AuditEventRepository audit;
    @Autowired CompanyRepository companies;
    @Autowired JobSlotRepository jobSlots;

    private AppUser tutor;
    private Project project;
    private Zone zone;
    private JobCategory category;

    private static Instant at(int y, int m, int d, int h, int min) {
        return LocalDateTime.of(y, m, d, h, min).atZone(TimeConfig.ZONE).toInstant();
    }

    @BeforeEach
    void setUp() {
        tutor = users.save(TestEntities.user("tutor1", Role.TUTOR, "00000000-0000-4000-8000-000000000001"));
        project = projects.save(TestEntities.project("PTIMER"));
        zone = zones.save(TestEntities.zone("ZTIMER"));
        category = categories.save(TestEntities.jobCategory("CTIMER"));
    }

    private Ticket accepted(TicketStatus status, boolean timerActive) {
        Ticket t = TestEntities.ticket(tutor, project, beneficiaries.save(TestEntities.beneficiary(tutor, zone)), category);
        t.setStatus(status);
        if (timerActive) {
            t.setTimerStartedAt(ACCEPTED);
            t.setTimerDeadlineAt(ACCEPTED.plus(Duration.ofDays(10)));
        }
        return tickets.saveAndFlush(t);
    }

    private long reminders() {
        return notifications.findByRecipientIdOrderByCreatedAtDescIdDesc(tutor.getId(), PageRequest.of(0, 50)).stream()
                .filter(n -> n.getType() == NotificationType.TIMER_REMINDER)
                .count();
    }

    @Test
    void promemoriaAl4oGiornoLavorativo_unaSolaVolta() {
        Ticket ticket = accepted(TicketStatus.PROPOSTA_ACCOLTA, true);

        // giovedì 5: è solo il 3° giorno lavorativo
        assertThat(timerService.sendDueReminders(at(2026, 11, 5, 23, 5))).isZero();
        // venerdì 6, primo controllo della giornata
        assertThat(timerService.sendDueReminders(at(2026, 11, 6, 0, 5))).isEqualTo(1);
        // il controllo successivo non lo ripete
        assertThat(timerService.sendDueReminders(at(2026, 11, 6, 1, 5))).isZero();

        assertThat(reminders()).isEqualTo(1);
        assertThat(tickets.findById(ticket.getId()).orElseThrow().getTimerReminderSentAt())
                .isEqualTo(at(2026, 11, 6, 0, 5));
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId()))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("TIMER_REMINDER");
                    assertThat(e.getCreatedBy()).isEqualTo("system");
                });
    }

    @Test
    void scadenza_riapertaMansioneLiberaNotificheENessunPromemoria() {
        AppUser operator = users.save(TestEntities.user("operatore.cc", Role.CALL_CENTER));
        Company company = companies.save(TestEntities.company("99900000301"));
        JobSlot slot = jobSlots.save(TestEntities.jobSlot(company, category, zone));
        Ticket ticket = accepted(TicketStatus.PROPOSTA_ACCOLTA, true);
        ticket.setAssignedCcOperator(operator);
        ticket.setJobSlot(slot);
        slot.blockFor(ticket);
        tickets.saveAndFlush(ticket);
        Instant deadline = ticket.getTimerDeadlineAt();

        assertThat(timerService.expireDue(deadline.minusSeconds(1))).isZero();
        // il controllo orario: prima le scadenze, poi i promemoria
        assertThat(timerService.expireDue(deadline)).isEqualTo(1);
        assertThat(timerService.sendDueReminders(deadline)).isZero();
        assertThat(timerService.expireDue(deadline.plusSeconds(3600))).isZero();

        Ticket saved = tickets.findById(ticket.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.RIAPERTO);
        assertThat(saved.getTimerStartedAt()).isNull();
        assertThat(saved.getTimerDeadlineAt()).isNull();
        assertThat(saved.getJobSlot()).isNull();
        assertThat(saved.getAssignedCcOperator()).isEqualTo(operator);
        assertThat(jobSlots.findById(slot.getId()).orElseThrow().getStatus()).isEqualTo(JobSlotStatus.LIBERA);
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId()))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getAction()).isEqualTo("TIMER_EXPIRE");
                    assertThat(e.getCreatedBy()).isEqualTo("system");
                });
        assertThat(notifications.findByRecipientIdOrderByCreatedAtDescIdDesc(tutor.getId(), PageRequest.of(0, 5)))
                .extracting(n -> n.getType()).containsExactly(NotificationType.TIMER_EXPIRED);
        assertThat(notifications.findByRecipientIdOrderByCreatedAtDescIdDesc(operator.getId(), PageRequest.of(0, 5)))
                .extracting(n -> n.getType()).containsExactly(NotificationType.TIMER_EXPIRED);
    }

    @Test
    void senzaTimerAttivoNessunPromemoria() {
        // appuntamento già fissato: il timer è fermo
        accepted(TicketStatus.APPUNTAMENTO, false);

        assertThat(timerService.sendDueReminders(at(2026, 11, 20, 9, 5))).isZero();
        assertThat(reminders()).isZero();
    }
}
