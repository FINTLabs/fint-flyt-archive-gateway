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
) {
    companion object {
        @JvmStatic
        fun <T> builder() = Builder<T>()
    }

    class Builder<T> {
        private var order: Int = 0
        private var elementConfig: ElementConfig? = null
        private var template: T? = null

        fun order(order: Int) = apply { this.order = order }

        fun elementConfig(elementConfig: ElementConfig?) = apply { this.elementConfig = elementConfig }

        fun template(template: T?) = apply { this.template = template }

        fun build() = ElementTemplate(order = order, elementConfig = elementConfig, template = template)
    }
}
