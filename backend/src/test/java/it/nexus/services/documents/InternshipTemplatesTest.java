package it.nexus.services.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;

import it.nexus.services.impl.InternshipDocumentServiceImpl;

/** I modelli segnaposto forniti (US-1004) usano solo segnaposto che il servizio sa compilare. */
class InternshipTemplatesTest {

    @ParameterizedTest
    @ValueSource(strings = {"CURRICULARE", "EXTRACURRICULARE", "INCLUSIONE", "SOCIO_TERAPEUTICO", "default"})
    void convenzione_soloSegnapostiNoti(String name) throws IOException {
        try (InputStream template = new ClassPathResource("templates/documents/convenzione/" + name + ".docx")
                .getInputStream()) {
            assertThat(DocxTemplate.placeholders(template))
                    .isNotEmpty()
                    .isSubsetOf(InternshipDocumentServiceImpl.PLACEHOLDERS)
                    .contains("NOME_TIROCINANTE", "GRIGLIA_ORARIA", "MONTE_ORE_TOTALE", "TIPO_TIROCINIO");
        }
    }
}
