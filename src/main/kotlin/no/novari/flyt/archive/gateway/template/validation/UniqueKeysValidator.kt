package no.novari.flyt.archive.gateway.template.validation

import no.novari.flyt.archive.gateway.template.model.ElementTemplate
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext
import org.hibernate.validator.internal.util.CollectionHelper

class UniqueKeysValidator : HibernateConstraintValidator<UniqueKeys, ObjectTemplate> {
    override fun isValid(
        value: ObjectTemplate?,
        hibernateConstraintValidatorContext: HibernateConstraintValidatorContext,
    ): Boolean {
        if (value == null) {
            return true
        }
        val duplicateKeys = findDuplicateKeys(value)
        if (duplicateKeys.isEmpty()) {
            return true
        }

        hibernateConstraintValidatorContext
            .addMessageParameter(
                UniqueKeys.DUPLICATE_KEYS_REF,
                duplicateKeys.joinToString(", ", prefix = "[", postfix = "]") { "'$it'" },
            ).withDynamicPayload(CollectionHelper.toImmutableList(duplicateKeys))

        return false
    }

    protected fun findDuplicateKeys(value: ObjectTemplate): List<String> {
        val checkedKeys = mutableSetOf<String>()
        return sequenceOf(
            value.valueTemplates,
            value.selectableValueTemplates,
            value.objectTemplates,
            value.objectCollectionTemplates,
        ).flatMap { it.asSequence() }
            .filterNotNull()
            .mapNotNull(ElementTemplate<*>::elementConfig)
            .mapNotNull { it.key }
            .filterNot(checkedKeys::add)
            .toList()
    }
}
