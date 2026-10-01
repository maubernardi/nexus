package it.nexus.services;

import static it.nexus.domain.enumeration.TicketStatus.IN_LAVORAZIONE;
import static it.nexus.domain.enumeration.TicketStatus.NUOVA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.domain.DomainFixtures;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.Role;
import it.nexus.domain.workflow.TicketTransition;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.repository.TicketStatusHistoryRepository;
import it.nexus.support.TestSecurity;
import it.nexus.web.errors.ConflictException;
import jakarta.persistence.EntityManager;

/** Regole del motore sul database reale: cronologia, transizioni non ammesse, blocco ottimistico. */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, DomainFixtures.class})
class TicketStateMachineIT {

    private static final TicketTransition TAKE = new TicketTransition("TEST_TAKE", Set.of(NUOVA), IN_LAVORAZIONE,
            Set.of(Role.CALL_CENTER));

    @Autowired TicketStateMachine stateMachine;
    @Autowired TicketRepository tickets;
    @Autowired TicketStatusHistoryRepository history;
    @Autowired DomainFixtures fixtures;
    @Autowired EntityManager em;
    @Autowired AuditEventRepository audit;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticket = fixtures.ticket(fixtures.graph());
        TestSecurity.authenticate("operatore.cc", Role.CALL_CENTER);
    }

    @AfterEach
    void tearDown() {
        TestSecurity.clear();
    }

    @Test
    void transizioneRiuscita_statoNuovoECronologiaConAutoreENota() {
        stateMachine.apply(ticket, TAKE, ticket.getVersion(), "Presa in carico");
        em.flush();
        em.clear();

        assertThat(tickets.findById(ticket.getId()).orElseThrow().getStatus()).isEqualTo(IN_LAVORAZIONE);
        assertThat(history.findByTicketIdOrderByCreatedAtAsc(ticket.getId()))
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.getFromStatus()).isEqualTo(NUOVA);
                    assertThat(row.getToStatus()).isEqualTo(IN_LAVORAZIONE);
                    assertThat(row.getNote()).isEqualTo("Presa in carico");
                    assertThat(row.getCreatedBy()).isEqualTo("operatore.cc");
                    assertThat(row.getCreatedAt()).isNotNull();
                });
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.TICKET, ticket.getId()))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getAction()).isEqualTo("TEST_TAKE");
                    assertThat(event.getChanges())
                            .containsEntry("status", java.util.Map.of("before", "NUOVA", "after", "IN_LAVORAZIONE"));
                    assertThat(event.getReason()).isEqualTo("Presa in carico");
                    assertThat(event.getCreatedBy()).isEqualTo("operatore.cc");
                });
    }

    @Test
    void secondaRichiestaDallaStessaVersione_409EUnaSolaRigaDiCronologia() {
        long versioneVista = ticket.getVersion();
        stateMachine.apply(ticket, TAKE, versioneVista, null);

        assertThatThrownBy(() -> stateMachine.apply(ticket, TAKE, versioneVista, null))
                .isInstanceOf(ConflictException.class);
        em.flush();
        em.clear();
        assertThat(tickets.findById(ticket.getId()).orElseThrow().getStatus()).isEqualTo(IN_LAVORAZIONE);
        assertThat(history.findByTicketIdOrderByCreatedAtAsc(ticket.getId())).hasSize(1);
    }

    @Test
    void transizioneNonAmmessa_409SenzaEffetti() {
        stateMachine.apply(ticket, TAKE, ticket.getVersion(), null);
        long versione = ticket.getVersion();

        assertThatThrownBy(() -> stateMachine.apply(ticket, TAKE, versione, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("IN_LAVORAZIONE");
        assertThat(history.findByTicketIdOrderByCreatedAtAsc(ticket.getId())).hasSize(1);
    }

    @Test
    void garaTraTransazioni_rilevataDalBloccoOttimistico() {
        // un'altra transazione ha già aggiornato il ticket dopo che lo abbiamo letto
        em.createNativeQuery("UPDATE ticket SET version = version + 1 WHERE id = :id")
                .setParameter("id", ticket.getId())
                .executeUpdate();

        assertThatThrownBy(() -> stateMachine.apply(ticket, TAKE, ticket.getVersion(), null))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
