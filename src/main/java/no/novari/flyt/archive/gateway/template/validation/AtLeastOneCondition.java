package no.novari.flyt.archive.gateway.template.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = AtLeastOneConditionValidator.class)
public @interface AtLeastOneCondition {

    String message() default "contains no predicate conditions";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
