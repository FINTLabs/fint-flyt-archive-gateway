package no.novari.flyt.archive.gateway.template.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueOrdersValidator::class])
annotation class UniqueOrders(
    val message: String = "contains duplicate orders: {$DUPLICATE_ORDERS_REF}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
) {
    companion object {
        const val DUPLICATE_ORDERS_REF = "duplicateOrders"
    }
}
