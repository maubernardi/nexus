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

/** Codice paese ISO 3166-1 alpha-2 in maiuscolo (es. IT); {@code null} ammesso. */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Constraint(validatedBy = IsoCountry.Validator.class)
public @interface IsoCountry {

    String message() default "Paese non valido (codice ISO 3166-1 a due lettere)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<IsoCountry, String> {
        private static final Set<String> CODES = Set.of(Locale.getISOCountries());

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || CODES.contains(value);
        }
    }
}
