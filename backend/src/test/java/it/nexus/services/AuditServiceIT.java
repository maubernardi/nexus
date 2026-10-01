package it.nexus.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;

import it.nexus.TestcontainersConfiguration;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.enumeration.AuditEntityType;

/** L'audit si scrive solo dentro la transazione dell'operazione che registra. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuditServiceIT {

    @Autowired AuditService auditService;

    @Test
    void senzaTransazione_errore() {
        assertThatThrownBy(() -> auditService.record(AuditEntityType.TICKET, 1L, "SUBMIT", AuditChanges.none(), null))
                .isInstanceOf(IllegalTransactionStateException.class);
    }
}
