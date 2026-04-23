package no.novari.flyt.archive.gateway.template.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueKeysValidator::class])
annotation class UniqueKeys(
    val message: String = "contains duplicate keys: {$DUPLICATE_KEYS_REF}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
) {
    companion object {
        const val DUPLICATE_KEYS_REF = "duplicateKeys"
    }
}
