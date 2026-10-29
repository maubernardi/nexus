package it.nexus.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Codice fiscale di persona fisica: 16 caratteri nella struttura ufficiale (anche con omocodia) e carattere di
 * controllo corretto. Maiuscole e minuscole indifferenti; {@code null} o vuoto ammessi (si usa con {@code @NotBlank}).
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Constraint(validatedBy = FiscalCode.Validator.class)
public @interface FiscalCode {

    String message() default "Codice fiscale non valido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<FiscalCode, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || value.isBlank() || FiscalCodes.isValid(value);
        }
    }
}
