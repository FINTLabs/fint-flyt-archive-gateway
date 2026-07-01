package no.novari.flyt.archive.gateway.template.validation

import no.novari.flyt.archive.gateway.template.model.ElementTemplate
import no.novari.flyt.archive.gateway.template.model.ObjectTemplate
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext
import org.hibernate.validator.internal.util.CollectionHelper

open class UniqueOrdersValidator : HibernateConstraintValidator<UniqueOrders, ObjectTemplate> {
    override fun isValid(
        value: ObjectTemplate?,
        hibernateConstraintValidatorContext: HibernateConstraintValidatorContext,
    ): Boolean {
        if (value == null) {
            return true
        }
        val duplicateOrders = findDuplicateOrders(value)
        if (duplicateOrders.isEmpty()) {
            return true
        }

        hibernateConstraintValidatorContext
            .addMessageParameter(
                UniqueOrders.DUPLICATE_ORDERS_REF,
                duplicateOrders.joinToString(", ", prefix = "[", postfix = "]") { "'$it'" },
            ).withDynamicPayload(CollectionHelper.toImmutableList(duplicateOrders))

        return false
    }

    protected fun findDuplicateOrders(value: ObjectTemplate): List<Int> {
        val checkedOrders = mutableSetOf<Int>()
        return sequenceOf(
            value.valueTemplates,
            value.selectableValueTemplates,
            value.objectTemplates,
            value.objectCollectionTemplates,
        ).flatMap { it.asSequence() }
            .mapNotNull(ElementTemplate<*>::order)
            .filterNot(checkedOrders::add)
            .toList()
    }
}
