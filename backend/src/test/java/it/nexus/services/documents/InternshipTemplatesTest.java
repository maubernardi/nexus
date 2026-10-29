package it.nexus.services.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;

import it.nexus.services.impl.InternshipDocumentServiceImpl;

/** I modelli segnaposto forniti (US-1004, US-1005) usano solo segnaposto che il servizio sa compilare. */
class InternshipTemplatesTest {

    @ParameterizedTest
    @CsvSource({
        "convenzione, CURRICULARE", "convenzione, EXTRACURRICULARE", "convenzione, INCLUSIONE",
        "convenzione, SOCIO_TERAPEUTICO", "convenzione, default",
        "progetto-formativo, CURRICULARE", "progetto-formativo, EXTRACURRICULARE", "progetto-formativo, INCLUSIONE",
        "progetto-formativo, SOCIO_TERAPEUTICO", "progetto-formativo, default"})
    void soloSegnapostiNoti(String folder, String name) throws IOException {
        try (InputStream template = new ClassPathResource("templates/documents/" + folder + "/" + name + ".docx")
                .getInputStream()) {
            assertThat(DocxTemplate.placeholders(template))
                    .isNotEmpty()
                    .isSubsetOf(InternshipDocumentServiceImpl.PLACEHOLDERS)
                    .contains("NOME_TIROCINANTE", "GRIGLIA_ORARIA", "MONTE_ORE_TOTALE", "TIPO_TIROCINIO");
        }
    }
}
