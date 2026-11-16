package it.nexus.services.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;

/** I modelli generici nel codice (US-1004, US-1005, US-1011) usano solo segnaposto che il servizio sa compilare. */
class InternshipTemplatesTest {

    @ParameterizedTest
    @CsvSource({
        "convenzione, CURRICULARE", "convenzione, EXTRACURRICULARE", "convenzione, INCLUSIONE",
        "convenzione, SOCIO_TERAPEUTICO", "convenzione, default",
        "progetto-formativo, CURRICULARE", "progetto-formativo, EXTRACURRICULARE", "progetto-formativo, INCLUSIONE",
        "progetto-formativo, SOCIO_TERAPEUTICO", "progetto-formativo, default", "relazione-finale, default"})
    void soloSegnapostiNoti(String folder, String name) throws IOException {
        try (InputStream template = new ClassPathResource("templates/documents/" + folder + "/" + name + ".docx")
                .getInputStream()) {
            assertThat(DocxTemplate.placeholders(template))
                    .isNotEmpty()
                    .isSubsetOf(DocumentPlaceholders.KNOWN)
                    .contains("NOME_TIROCINANTE", "TIPO_TIROCINIO", "INTESTAZIONE_PROGETTO", "PROMOTORE");
        }
    }

    @Test
    void segnapostiUniciEDescritti() {
        assertThat(DocumentPlaceholders.ALL).extracting(DocumentPlaceholders.Placeholder::name)
                .doesNotHaveDuplicates()
                .allMatch(name -> name.matches("[A-Z0-9_]+"));
        assertThat(DocumentPlaceholders.ALL)
                .allMatch(p -> !p.group().isBlank() && !p.description().isBlank());
        assertThat(DocumentPlaceholders.KNOWN).hasSameSizeAs(DocumentPlaceholders.ALL);
    }
}
