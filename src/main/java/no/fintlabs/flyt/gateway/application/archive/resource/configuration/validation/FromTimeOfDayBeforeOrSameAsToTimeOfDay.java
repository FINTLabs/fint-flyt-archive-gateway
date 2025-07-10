package no.fintlabs.flyt.gateway.application.archive.resource.configuration.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FromTimeOfDayBeforeOrSameAsToTimeOfDayValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FromTimeOfDayBeforeOrSameAsToTimeOfDay {

    String message() default
            "'From time of day' must be smaller than or equal to 'To time of day'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
