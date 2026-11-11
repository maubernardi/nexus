package it.nexus.web.rest.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import it.nexus.TestcontainersConfiguration;
import it.nexus.config.security.MockHeaderAuthenticationFilter;
import it.nexus.domain.InternshipType;
import it.nexus.domain.Project;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.AuditEventRepository;
import it.nexus.repository.InternshipTypeRepository;
import it.nexus.support.TestDocx;
import it.nexus.support.WorkflowScenarios;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, WorkflowScenarios.class})
/** Modelli Word caricati dall'ADMIN (US-1010): controlli al caricamento, sostituzione, disattivazione e audit. */
class DocumentTemplateIT {

    private static final String USER = MockHeaderAuthenticationFilter.HEADER;
    private static final String BASE = "/api/v1/document-templates";
    private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    /** Comune di Firenze, tra gli enti iniziali di V34. */
    private static final String COMUNE_DI_FIRENZE = TsidMapper.toExternal(893700000000000351L);

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired WorkflowScenarios scenarios;
    @Autowired InternshipTypeRepository internshipTypes;
    @Autowired AuditEventRepository audit;
    @Autowired JdbcTemplate jdbc;

    private Project project;
    private InternshipType type;

    @BeforeEach
    void setUp() {
        scenarios.actors();
        project = scenarios.project("GOL");
        type = internshipTypes.save(new InternshipType("T_DT_" + project.getId(), "Tipo di prova " + project.getId()));
        em.flush();
    }

    private static MockMultipartFile docx(String name, byte[] content) {
        return new MockMultipartFile("file", name, DOCX, content);
    }

    private ResultActions upload(String user, MockMultipartFile file, Map<String, String> params) throws Exception {
        var request = multipart(BASE).file(file).header(USER, user);
        params.forEach(request::param);
        return mockMvc.perform(request);
    }

    /** Campi validi: convenzione per il progetto e il tipo di prova, senza ente. */
    private Map<String, String> fields(String name) {
        return Map.of("document", "CONVENZIONE", "projectId", TsidMapper.toExternal(project.getId()),
                "internshipTypeId", TsidMapper.toExternal(type.getId()), "name", name);
    }

    private String uploadValid(String name) throws Exception {
        String body = upload("admin", docx("convenzione.docx", TestDocx.of("Convenzione per {NOME_TIROCINANTE}")),
                fields(name))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void caricamento_elenco_fileEAudit() throws Exception {
        byte[] content = TestDocx.of("Convenzione per {NOME_TIROCINANTE}", "Dal {DATA_INIZIO} al {DATA_FINE}");
        var params = new HashMap<>(fields("  Convenzione GOL  "));
        params.put("careAgencyId", COMUNE_DI_FIRENZE);
        String body = upload("admin", docx("C:\\modelli\\Convenzione GOL.docx", content), params)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.document").value("CONVENZIONE"))
                .andExpect(jsonPath("$.project.name").value(project.getName()))
                .andExpect(jsonPath("$.internshipType.code").value(type.getCode()))
                .andExpect(jsonPath("$.careAgency.name").value("Comune di Firenze"))
                .andExpect(jsonPath("$.name").value("Convenzione GOL"))
                .andExpect(jsonPath("$.fileName").value("Convenzione GOL.docx"))
                .andExpect(jsonPath("$.sizeBytes").value(content.length))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.uploadedAt").exists())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");

        mockMvc.perform(get(BASE).header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Convenzione GOL")));
        mockMvc.perform(get(BASE + "/" + id + "/file").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DOCX))
                .andExpect(content().bytes(content));
        mockMvc.perform(get(BASE + "/placeholders").header(USER, "admin"))
                .andExpect(jsonPath("$", hasItem("NOME_TIROCINANTE")));

        Map<String, Object> changes = audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(
                AuditEntityType.DOCUMENT_TEMPLATE, TsidMapper.toInternal(id)).getFirst().getChanges();
        assertThat(changes.get("document")).extracting("after").isEqualTo("CONVENZIONE");
        assertThat(changes.get("project")).extracting("after").isEqualTo(project.getCode());
        assertThat(changes.get("careAgency")).extracting("after").isEqualTo("COMUNE_DI_FIRENZE");
        assertThat(changes.get("sizeBytes")).extracting("after").isEqualTo(content.length);
    }

    @Test
    void controlliAlCaricamento_fileSegnapostoECondizioni() throws Exception {
        upload("admin", docx("convenzione.docx", TestDocx.of("{NOME_TIROCINANTE} {CUP}", "{DATA_STIPULA}")),
                fields("Convenzione"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("file"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Il modello usa segnaposto che NEXUS non conosce: {CUP}, {DATA_STIPULA}"));
        String notDocx = "Il file deve essere un documento Word .docx: un .doc del vecchio formato va salvato come .docx";
        upload("admin", docx("convenzione.pdf", "%PDF-1.7 finto".getBytes(StandardCharsets.US_ASCII)),
                fields("Convenzione"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value(notDocx));
        // un archivio ZIP che non è un documento Word
        upload("admin", docx("convenzione.docx", new byte[] {'P', 'K', 3, 4, 0, 0, 0}), fields("Convenzione"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value(notDocx));
        upload("admin", docx("vuoto.docx", new byte[0]), fields("Convenzione"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Scegli il file del modello"));

        byte[] valid = TestDocx.of("{NOME_TIROCINANTE}");
        var noDocument = new HashMap<>(fields("Convenzione"));
        noDocument.remove("document");
        upload("admin", docx("c.docx", valid), noDocument)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("document"));
        upload("admin", docx("c.docx", valid), Map.of("document", "CONVENZIONE",
                        "projectId", TsidMapper.toExternal(project.getId()),
                        "internshipTypeId", TsidMapper.toExternal(type.getId()), "name", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
        var unknownAgency = new HashMap<>(fields("Convenzione"));
        unknownAgency.put("careAgencyId", "0ABCDEFGHJKMN");
        upload("admin", docx("c.docx", valid), unknownAgency)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("careAgencyId"));

        // il progetto ammette solo un altro tipo (US-115)
        project.getAllowedInternshipTypes().add(internshipTypes.save(new InternshipType("T_DT2_" + project.getId(),
                "Altro tipo " + project.getId())));
        em.flush();
        upload("admin", docx("c.docx", valid), fields("Convenzione"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("internshipTypeId"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("Il progetto %s non ammette il tipo %s".formatted(project.getName(), type.getName())));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM document_template", Long.class)).isZero();
    }

    @Test
    void sostituzioneDelFile_disattivazioneEVersione() throws Exception {
        String id = uploadValid("Convenzione GOL");
        Long oldFile = jdbc.queryForObject("SELECT file_id FROM document_template WHERE id = ?", Long.class,
                TsidMapper.toInternal(id));
        byte[] replacement = TestDocx.of("Convenzione corretta per {NOME_TIROCINANTE}", "{DATA_DOCUMENTO}");

        mockMvc.perform(multipart(HttpMethod.PUT, BASE + "/" + id + "/file")
                        .file(docx("convenzione-2.docx", replacement)).param("version", "0").header(USER, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("convenzione-2.docx"))
                .andExpect(jsonPath("$.sizeBytes").value(replacement.length))
                .andExpect(jsonPath("$.version").value(1));
        // il file vecchio non serve più: i documenti già scaricati non sono in NEXUS
        assertThat(jdbc.queryForObject("SELECT count(*) FROM stored_file WHERE id = ?", Long.class, oldFile)).isZero();
        mockMvc.perform(get(BASE + "/" + id + "/file").header(USER, "admin"))
                .andExpect(content().bytes(replacement));
        // stessi controlli del caricamento, versione superata
        mockMvc.perform(multipart(HttpMethod.PUT, BASE + "/" + id + "/file")
                        .file(docx("x.docx", TestDocx.of("{CUP}"))).param("version", "1").header(USER, "admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("file"));
        mockMvc.perform(multipart(HttpMethod.PUT, BASE + "/" + id + "/file")
                        .file(docx("x.docx", replacement)).param("version", "0").header(USER, "admin"))
                .andExpect(status().isConflict());

        mockMvc.perform(put(BASE + "/" + id + "/active").header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false, \"version\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(put(BASE + "/" + id + "/active").header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true, \"version\": 1}"))
                .andExpect(status().isConflict());
        mockMvc.perform(put(BASE + "/" + id + "/active").header(USER, "admin").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true, \"version\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        assertThat(audit.findByEntityTypeAndEntityIdOrderByCreatedAtAscIdAsc(AuditEntityType.DOCUMENT_TEMPLATE,
                TsidMapper.toInternal(id)))
                .extracting(e -> e.getAction())
                .containsExactly("TEMPLATE_UPLOADED", "TEMPLATE_FILE_REPLACED", "TEMPLATE_DEACTIVATED",
                        "TEMPLATE_ACTIVATED");
    }

    @Test
    void soloLAdmin() throws Exception {
        String id = uploadValid("Convenzione GOL");
        for (String user : new String[] {"tutor1", "operatore.cc"}) {
            mockMvc.perform(get(BASE).header(USER, user)).andExpect(status().isForbidden());
            mockMvc.perform(get(BASE + "/" + id + "/file").header(USER, user)).andExpect(status().isForbidden());
            upload(user, docx("c.docx", TestDocx.of("{NOME_TIROCINANTE}")), fields("Altro"))
                    .andExpect(status().isForbidden());
        }
    }
}
