package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElementTemplate<T>(
    @field:PositiveOrZero
    val order: Int = 0,
    @field:Valid
    @field:NotNull
    val elementConfig: ElementConfig? = null,
    @field:Valid
    @field:NotNull
    val template: T? = null,
)
