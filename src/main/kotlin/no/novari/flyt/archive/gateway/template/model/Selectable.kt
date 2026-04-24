package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Selectable(
    @field:NotBlank
    val displayName: String? = null,
    @field:NotNull
    val value: String? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var displayName: String? = null
        private var value: String? = null

        fun displayName(displayName: String?) = apply { this.displayName = displayName }

        fun value(value: String?) = apply { this.value = value }

        fun build() = Selectable(displayName = displayName, value = value)
    }
}
