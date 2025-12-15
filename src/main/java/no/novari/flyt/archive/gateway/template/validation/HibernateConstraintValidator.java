package no.novari.flyt.archive.gateway.template.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.lang.annotation.Annotation;

public interface HibernateConstraintValidator<A extends Annotation, T> extends ConstraintValidator<A, T> {

    @Override
    default boolean isValid(T value, ConstraintValidatorContext constraintValidatorContext) {
        if (constraintValidatorContext instanceof HibernateConstraintValidatorContext) {
            HibernateConstraintValidatorContext hibernateConstraintValidatorContext =
                    constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class);
            return isValid(value, hibernateConstraintValidatorContext);
        }
        throw new IllegalStateException("Validator is not HibernateConstraintValidatorContext");
    }

    boolean isValid(T value, HibernateConstraintValidatorContext hibernateConstraintValidatorContext);

}
