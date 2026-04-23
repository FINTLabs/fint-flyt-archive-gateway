package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectionTemplate<T>(
    @field:NotNull
    @field:Valid
    val elementTemplate: T,
) {
    companion object {
        @JvmStatic
        fun <T> builder() = Builder<T>()
    }

    class Builder<T> {
        private var elementTemplate: T? = null

        fun elementTemplate(elementTemplate: T) = apply { this.elementTemplate = elementTemplate }

        fun build() = CollectionTemplate(requireNotNull(elementTemplate) { "elementTemplate is required" })
    }
}
