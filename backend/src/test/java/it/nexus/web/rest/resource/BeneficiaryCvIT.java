package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.AuditEvent;
import it.nexus.domain.Beneficiary;
import it.nexus.domain.Project;
import it.nexus.domain.TestEntities;
import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.BeneficiaryRepository;
import it.nexus.repository.TicketRepository;
import it.nexus.services.BeneficiaryRetentionService;
import it.nexus.support.WorkflowScenarios;
import it.nexus.support.WorkflowScenarios.Actors;
import it.nexus.support.WorkflowScenarios.Place;

/** Curriculum del beneficiario (US-205): PDF fino a 5 MB, gestito dal Tutor, scaricato da chi vede il beneficiario. */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
class BeneficiaryCvIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final byte[] PDF = "%PDF-1.7\nCV di prova\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);

    @Autowired MockMvc mockMvc;
    @Autowired WorkflowScenarios scenarios;
    @Autowired BeneficiaryRepository beneficiaries;
    @Autowired TicketRepository tickets;
    @Autowired AuditEventRepository audit;
    @Autowired BeneficiaryRetentionService retention;
    @Autowired JdbcTemplate jdbc;

    private Actors actors;
    private Place place;
    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        actors = scenarios.actors();
        place = scenarios.place();
        beneficiary = scenarios.beneficiary(actors.tutor1(), place);
    }

    private String url() {
        return "/api/v1/beneficiaries/" + TsidMapper.toExternal(beneficiary.getId()) + "/cv";
    }

    private ResultActions upload(String user, String name, byte[] content) throws Exception {
        return mockMvc.perform(multipart(HttpMethod.PUT, url())
                .file(new MockMultipartFile("file", name, "application/pdf", content)).header(USER, user));
    }

    private int files() {
        return jdbc.queryForObject("SELECT count(*) FROM stored_file_content", Integer.class);
    }

    private List<String> actions() {
        return audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BENEFICIARY, beneficiary.getId())
                .stream().map(AuditEvent::getAction).toList();
    }

    @Test
    void ilTutorCaricaIlCv_eChiVedeIlBeneficiarioLoScarica() throws Exception {
        upload("tutor1", "C:\\Documenti\\CV Mario Rossi.pdf", PDF)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvFileName").value("CV Mario Rossi.pdf"))
                .andExpect(jsonPath("$.cvSizeBytes").value(PDF.length));

        for (String user : List.of("tutor1", "operatore.cc", "admin")) {
            mockMvc.perform(get(url()).header(USER, user))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/pdf"))
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment")))
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(content().bytes(PDF));
        }
        // nell'audit solo la dimensione, non il nome del file (può contenere il nome della persona)
        assertThat(actions()).containsExactly("CV_UPLOADED");
        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.BENEFICIARY,
                beneficiary.getId()).getLast().getChanges().toString()).doesNotContain("Mario");
    }

    @Test
    void unTutorDiUnAltroProgettoNonLoVede_eSoloIlProprietarioLoGestisce() throws Exception {
        upload("tutor1", "cv.pdf", PDF).andExpect(status().isOk());

        mockMvc.perform(get(url()).header(USER, "tutor2")).andExpect(status().isNotFound());
        upload("operatore.cc", "cv.pdf", PDF).andExpect(status().isForbidden());
        mockMvc.perform(delete(url()).header(USER, "admin")).andExpect(status().isForbidden());

        // un Tutor del progetto della segnalazione lo vede (US-204), ma non lo cambia
        Project gol = scenarios.project("GOL", actors.tutor1(), actors.tutor2());
        Ticket ticket = TestEntities.ticket(actors.tutor1(), gol, beneficiary, place.category());
        tickets.saveAndFlush(ticket);
        mockMvc.perform(get(url()).header(USER, "tutor2")).andExpect(status().isOk());
        upload("tutor2", "cv.pdf", PDF).andExpect(status().isForbidden());
    }

    @Test
    void soloPdfFinoA5Mb() throws Exception {
        upload("tutor1", "cv.pdf", "non sono un pdf".getBytes(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("file"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Il CV deve essere un file PDF"));
        byte[] big = Arrays.copyOf(PDF, 5 * 1024 * 1024 + 1);
        upload("tutor1", "cv.pdf", big)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Il CV supera i 5 MB"));
        upload("tutor1", "cv.pdf", new byte[0]).andExpect(status().isBadRequest());
        assertThat(files()).isZero();
        // il nome non conta: un PDF con un altro nome diventa .pdf
        upload("tutor1", "curriculum", PDF).andExpect(jsonPath("$.cvFileName").value("curriculum.pdf"));
    }

    @Test
    void sostituito_eTolto_senzaFileRimasti() throws Exception {
        upload("tutor1", "vecchio.pdf", PDF).andExpect(status().isOk());
        upload("tutor1", "nuovo.pdf", PDF).andExpect(jsonPath("$.cvFileName").value("nuovo.pdf"));
        assertThat(files()).isEqualTo(1);

        mockMvc.perform(delete(url()).header(USER, "tutor1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvFileName").doesNotExist());
        assertThat(files()).isZero();
        mockMvc.perform(get(url()).header(USER, "tutor1")).andExpect(status().isNotFound());
        assertThat(actions()).containsExactly("CV_UPLOADED", "CV_REPLACED", "CV_REMOVED");
    }

    @Test
    void anonimizzato_ilCvSiCancellaENonSiCaricaPiu() throws Exception {
        upload("tutor1", "cv.pdf", PDF).andExpect(status().isOk());
        Instant old = Instant.now().minusSeconds(3L * 365 * 24 * 3600);
        jdbc.update("UPDATE beneficiary SET updated_at = ? WHERE id = ?", java.sql.Timestamp.from(old),
                beneficiary.getId());
        retention.review(Instant.now());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/beneficiaries/" + TsidMapper.toExternal(beneficiary.getId()) + "/anonymize")
                        .header(USER, "tutor1"))
                .andExpect(status().isNoContent());

        assertThat(files()).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM stored_file", Integer.class)).isZero();
        upload("tutor1", "cv.pdf", PDF).andExpect(status().isBadRequest());
    }
}
