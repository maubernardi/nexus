package it.nexus.services.mail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.mapper.TsidMapper;
import it.nexus.services.NotificationService;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;

/** Ogni notifica in app pubblica l'evento da cui, dopo il commit, parte l'email (US-1202). */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@RecordApplicationEvents
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class NotificationEmailIT {

    @Autowired WorkflowScenarios scenarios;
    @Autowired NotificationService notificationService;
    @Autowired ApplicationEvents events;

    @Test
    void laNotificaPubblicaLEventoConIndirizzoESegnalazione() {
        Actors a = scenarios.actors();
        Project project = scenarios.project("GOL", a.tutor1());
        Ticket ticket = scenarios.appointmentTicket(a.tutor1(), project,
                scenarios.freeSlot(scenarios.place(), "Aiuto cuoco"), a.operator());

        notificationService.notify(a.operator(), NotificationType.INTERNSHIP_STARTED, ticket);

        assertThat(events.stream(NotificationCreatedEvent.class)).singleElement().satisfies(e -> {
            assertThat(e.notificationId()).isNotNull();
            assertThat(e.recipientEmail()).isEqualTo(a.operator().getEmail());
            assertThat(e.type()).isEqualTo(NotificationType.INTERNSHIP_STARTED);
            assertThat(e.ticketId()).isEqualTo(TsidMapper.toExternal(ticket.getId()));
            assertThat(e.ticketNumber()).isEqualTo(ticket.getNumber());
        });
    }
}
