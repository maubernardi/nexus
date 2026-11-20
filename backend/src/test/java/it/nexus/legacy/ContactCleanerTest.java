package it.nexus.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;

/** Recapiti con le forme viste nel file vero (US-1309), con dati inventati. */
class ContactCleanerTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private record EmailField(@Email String value) {
    }

    @ParameterizedTest(name = "«{0}» → «{1}»")
    @CsvSource(delimiter = '|', nullValues = "-", textBlock = """
            055 123456              | 055 123456  | false
            +39 (055) 123.456/7     | +39 (055) 123.456/7 | false
            333 1234567 (sera)      | 333 1234567 | true
            055 1234567 int. 3      | 055 1234567 | true
            333 1234567*            | 333 1234567 | true
            non disponibile         | -           | true
            055 12 int              | -           | true
            """)
    void telefono(String raw, String value, boolean kept) {
        ContactCleaner.Phone phone = ContactCleaner.phone(raw);
        assertThat(phone.value()).isEqualTo(value);
        assertThat(phone.original()).isEqualTo(kept ? raw : null);
    }

    @Test
    void telefonoSuDueRighe_laPrimaNelCampo() {
        ContactCleaner.Phone phone = ContactCleaner.phone("055 123456\n055 654321");
        assertThat(phone.value()).isEqualTo("055 123456");
        assertThat(phone.original()).isEqualTo("055 123456\n055 654321");
    }

    @Test
    void telefonoVuoto() {
        assertThat(ContactCleaner.phone("  ")).isEqualTo(new ContactCleaner.Phone(null, null));
    }

    @ParameterizedTest(name = "«{0}»")
    @CsvSource(delimiter = '|', nullValues = "-", textBlock = """
            info@ditta.test#http://info@ditta.test#             | info@ditta.test                  | -
            Info@Ditta.test#mailto://info@ditta.test#           | Info@Ditta.test                  | -
            info@ditta.test                                     | info@ditta.test                  | -
            info@ditta.test.#http://info@ditta.test.#           | info@ditta.test                  | -
            mario.rossi@ditta-due.test#https://mario.rossi@ditta-due.test# | mario.rossi@ditta-due.test | -
            www.ditta.test#http://www.ditta.test#               | -                                | www.ditta.test
            info@ditta#http://info@ditta#                       | -                                | info@ditta
            info@ditta.test scrivere qui#http://info@ditta.test# | info@ditta.test                 | info@ditta.test scrivere qui
            """)
    void email(String raw, String first, String original) {
        ContactCleaner.Emails emails = ContactCleaner.emails(raw);
        assertThat(emails.valid()).isEqualTo(first == null ? List.of() : List.of(first));
        assertThat(emails.original()).isEqualTo(original);
        // quello che entra nel campo si salva: passa anche la validazione del modulo
        emails.valid().forEach(e -> assertThat(VALIDATOR.validate(new EmailField(e))).isEmpty());
    }

    @Test
    void dueIndirizziSuDueRighe() {
        ContactCleaner.Emails emails =
                ContactCleaner.emails("a@uno.test\nb@due.test#http://a@uno.test\nb@due.test#");
        assertThat(emails.valid()).containsExactly("a@uno.test", "b@due.test");
        assertThat(emails.original()).isNull();
    }
}
