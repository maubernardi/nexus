package it.nexus.services;

import static it.nexus.domain.enumeration.TicketStatus.IN_LAVORAZIONE;
import static it.nexus.domain.enumeration.TicketStatus.NUOVA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import it.nexus.domain.Ticket;
import it.nexus.domain.TicketStatusHistory;
import it.nexus.domain.TestEntities;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.domain.workflow.TicketTransition;
import it.nexus.domain.workflow.TicketTransitions;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.services.impl.TicketStateMachineImpl;
import it.nexus.support.TestSecurity;
import it.nexus.web.errors.ConflictException;

@ExtendWith(MockitoExtension.class)
class TicketStateMachineTest {

    private static final TicketTransition TAKE = new TicketTransition("TEST_TAKE", Set.of(NUOVA), IN_LAVORAZIONE,
            Set.of(Role.CALL_CENTER));

    @Mock TicketRepository tickets;
    @Mock TicketStatusHistoryRepository history;
    @InjectMocks TicketStateMachineImpl stateMachine;

    @AfterEach
    void tearDown() {
        TestSecurity.clear();
    }

    private static Ticket ticketIn(TicketStatus status) {
        Ticket ticket = TestEntities.ticket(null, null, null, null);
        ticket.setStatus(status);
        return ticket;
    }

    @Test
    void create_impostaStatoInizialeEScriveLaCronologiaSenzaStatoDiPartenza() {
        TestSecurity.authenticate("tutor1", Role.TUTOR);
        when(tickets.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        Ticket created = stateMachine.create(ticketIn(null), TicketTransitions.SUBMIT, "  ");

        assertThat(created.getStatus()).isEqualTo(NUOVA);
        ArgumentCaptor<TicketStatusHistory> row = ArgumentCaptor.forClass(TicketStatusHistory.class);
        verify(history).save(row.capture());
        assertThat(row.getValue().getFromStatus()).isNull();
        assertThat(row.getValue().getToStatus()).isEqualTo(NUOVA);
        assertThat(row.getValue().getNote()).isNull();
    }

    @Test
    void create_ruoloNonAmmesso_accessoNegatoSenzaSalvare() {
        TestSecurity.authenticate("operatore.cc", Role.CALL_CENTER);

        assertThatThrownBy(() -> stateMachine.create(ticketIn(null), TicketTransitions.SUBMIT, null))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(tickets, history);
    }

    @Test
    void apply_versioneSuperata_conflittoSenzaModifiche() {
        TestSecurity.authenticate("operatore.cc", Role.CALL_CENTER);
        Ticket ticket = ticketIn(NUOVA);

        assertThatThrownBy(() -> stateMachine.apply(ticket, TAKE, ticket.getVersion() + 1, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("modificato nel frattempo");
        assertThat(ticket.getStatus()).isEqualTo(NUOVA);
        verifyNoInteractions(tickets, history);
    }

    @Test
    void apply_statoDiPartenzaNonAmmesso_conflittoSenzaModifiche() {
        TestSecurity.authenticate("operatore.cc", Role.CALL_CENTER);
        Ticket ticket = ticketIn(IN_LAVORAZIONE);

        assertThatThrownBy(() -> stateMachine.apply(ticket, TAKE, ticket.getVersion(), null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("IN_LAVORAZIONE");
        assertThat(ticket.getStatus()).isEqualTo(IN_LAVORAZIONE);
        verifyNoInteractions(tickets, history);
    }

    @Test
    void apply_ruoloVerificatoPrimaDelloStato() {
        // un Tutor non deve poter scoprire lo stato di un ticket tramite messaggi di conflitto
        TestSecurity.authenticate("tutor1", Role.TUTOR);

        assertThatThrownBy(() -> stateMachine.apply(ticketIn(IN_LAVORAZIONE), TAKE, 0, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void apply_riuscita_cambiaStatoEScriveCronologiaConNota() {
        TestSecurity.authenticate("operatore.cc", Role.CALL_CENTER);
        when(tickets.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        Ticket ticket = ticketIn(NUOVA);

        stateMachine.apply(ticket, TAKE, ticket.getVersion(), " Presa in carico ");

        assertThat(ticket.getStatus()).isEqualTo(IN_LAVORAZIONE);
        ArgumentCaptor<TicketStatusHistory> row = ArgumentCaptor.forClass(TicketStatusHistory.class);
        verify(history).save(row.capture());
        assertThat(row.getValue().getFromStatus()).isEqualTo(NUOVA);
        assertThat(row.getValue().getToStatus()).isEqualTo(IN_LAVORAZIONE);
        assertThat(row.getValue().getNote()).isEqualTo("Presa in carico");
    }
}
