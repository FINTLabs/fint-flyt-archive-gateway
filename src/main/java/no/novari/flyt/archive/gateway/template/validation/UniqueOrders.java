package no.novari.flyt.archive.gateway.template.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = UniqueOrdersValidator.class)
public @interface UniqueOrders {

    String DUPLICATE_ORDERS_REF = "duplicateOrders";

    String message() default "contains duplicate orders: {" + DUPLICATE_ORDERS_REF + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
