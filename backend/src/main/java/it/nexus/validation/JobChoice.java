package it.nexus.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import it.nexus.domain.dto.TicketCreateDTO;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Mansione richiesta della segnalazione: una tipologia del catalogo oppure una nuova tipologia descritta dal Tutor
 * (US-302), esattamente una delle due. L'errore è sul campo {@code jobCategoryId}, insieme agli altri del modulo.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy = JobChoice.Validator.class)
public @interface JobChoice {

    String message() default "Seleziona la mansione richiesta o descrivine una nuova";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<JobChoice, TicketCreateDTO> {
        @Override
        public boolean isValid(TicketCreateDTO dto, ConstraintValidatorContext context) {
            boolean fromCatalog = dto.jobCategoryId() != null && !dto.jobCategoryId().isBlank();
            boolean described = dto.jobFreeText() != null && !dto.jobFreeText().isBlank();
            if (fromCatalog != described) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(fromCatalog
                            ? "Scegli una tipologia dell'elenco oppure descrivine una nuova, non entrambe"
                            : context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("jobCategoryId")
                    .addConstraintViolation();
            return false;
        }
    }
}
