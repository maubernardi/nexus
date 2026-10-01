package it.nexus.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Year;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/** Anno di nascita plausibile: dal 1900 all'anno corrente ({@code null} ammesso: si usa con {@code @NotNull}). */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Constraint(validatedBy = BirthYear.Validator.class)
public @interface BirthYear {

    String message() default "L'anno di nascita deve essere compreso tra il 1900 e l'anno corrente";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<BirthYear, Integer> {
        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            return value == null || (value >= 1900 && value <= Year.now().getValue());
        }
    }
}
