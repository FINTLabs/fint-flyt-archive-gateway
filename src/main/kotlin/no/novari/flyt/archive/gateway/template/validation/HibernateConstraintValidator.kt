package no.novari.flyt.archive.gateway.template.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext

interface HibernateConstraintValidator<A : Annotation, T> : ConstraintValidator<A, T> {
    override fun isValid(
        value: T?,
        constraintValidatorContext: ConstraintValidatorContext,
    ): Boolean {
        if (constraintValidatorContext is HibernateConstraintValidatorContext) {
            return isValid(value, constraintValidatorContext.unwrap(HibernateConstraintValidatorContext::class.java))
        }
        throw IllegalStateException("Validator is not HibernateConstraintValidatorContext")
    }

    fun isValid(
        value: T?,
        hibernateConstraintValidatorContext: HibernateConstraintValidatorContext,
    ): Boolean
}
