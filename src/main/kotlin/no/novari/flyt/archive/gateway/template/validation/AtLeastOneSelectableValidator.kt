package no.novari.flyt.archive.gateway.template.validation

import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext

class AtLeastOneSelectableValidator : HibernateConstraintValidator<AtLeastOneSelectable, SelectableValueTemplate> {
    override fun isValid(
        value: SelectableValueTemplate?,
        hibernateConstraintValidatorContext: HibernateConstraintValidatorContext,
    ): Boolean = value == null || getNumberOfSelectablesAndSelectablesSources(value) > 0

    private fun getNumberOfSelectablesAndSelectablesSources(value: SelectableValueTemplate): Int {
        val numberOfSelectables = value.selectables?.size ?: 0
        val numberOfSelectablesSources = value.selectablesSources?.size ?: 0
        return numberOfSelectables + numberOfSelectablesSources
    }
}
