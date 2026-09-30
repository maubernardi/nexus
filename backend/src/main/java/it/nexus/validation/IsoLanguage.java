package it.nexus.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/** Codice lingua ISO 639-1 in minuscolo (es. it); {@code null} ammesso. */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Constraint(validatedBy = IsoLanguage.Validator.class)
public @interface IsoLanguage {

    String message() default "Lingua non valida (codice ISO 639-1 a due lettere)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<IsoLanguage, String> {
        private static final Set<String> CODES = Set.of(Locale.getISOLanguages());

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || CODES.contains(value);
        }
    }
}
