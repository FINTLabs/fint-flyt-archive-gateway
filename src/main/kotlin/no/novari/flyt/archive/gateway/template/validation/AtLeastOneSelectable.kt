package no.novari.flyt.archive.gateway.template.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AtLeastOneSelectableValidator::class])
annotation class AtLeastOneSelectable(
    val message: String = "contains no selectables or selectable sources",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
