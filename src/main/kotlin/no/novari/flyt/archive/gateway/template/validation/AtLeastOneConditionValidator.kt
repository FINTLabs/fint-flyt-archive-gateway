package no.novari.flyt.archive.gateway.template.validation

import no.novari.flyt.archive.gateway.template.model.ValuePredicate
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext

class AtLeastOneConditionValidator : HibernateConstraintValidator<AtLeastOneCondition, ValuePredicate> {
    override fun isValid(
        value: ValuePredicate?,
        hibernateConstraintValidatorContext: HibernateConstraintValidatorContext,
    ): Boolean = value?.defined != null || value?.value != null || value?.notValue != null
}
