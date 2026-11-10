package it.nexus.domain;

import static it.nexus.domain.DomainConstraintsIT.assertViolates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.support.WorkflowScenarios;
import jakarta.persistence.EntityManager;

/** Vincoli di contatti e case manager del beneficiario (US-206), verificati per nome su PostgreSQL. */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class BeneficiaryCareConstraintsIT {

    @Autowired JdbcTemplate jdbc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired EntityManager em;

    private Long id;

    @BeforeEach
    void setUp() {
        WorkflowScenarios.Actors actors = scenarios.actors();
        id = scenarios.beneficiary(actors.tutor1(), scenarios.place()).getId();
        em.flush();
    }

    @ParameterizedTest
    @CsvSource({
        "email, non-una-mail, ck_beneficiary_email",
        "phone, chiamami, ck_beneficiary_phone",
        "case_manager_email, lucia@, ck_beneficiary_case_manager_email",
        "case_manager_phone, 055-abc, ck_beneficiary_case_manager_phone",
        "case_manager_name, '  ', ck_beneficiary_case_manager_name",
    })
    void valoreFuoriFormato(String column, String value, String constraint) {
        assertViolates(constraint,
                () -> jdbc.update("UPDATE beneficiary SET " + column + " = ? WHERE id = ?", value, id));
    }

    @Test
    void enteInesistente() {
        assertViolates("fk_beneficiary_care_agency",
                () -> jdbc.update("UPDATE beneficiary SET care_agency_id = 1 WHERE id = ?", id));
    }
}
