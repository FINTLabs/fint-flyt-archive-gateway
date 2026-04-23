package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElementConfig(
    @field:NotBlank
    @field:Pattern(regexp = "[^.]*")
    val key: String? = null,
    @field:NotBlank
    val displayName: String? = null,
    val description: String? = null,
    @field:Valid
    val showDependency: Dependency? = null,
    @field:Valid
    val enableDependency: Dependency? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var key: String? = null
        private var displayName: String? = null
        private var description: String? = null
        private var showDependency: Dependency? = null
        private var enableDependency: Dependency? = null

        fun key(key: String?) = apply { this.key = key }

        fun displayName(displayName: String?) = apply { this.displayName = displayName }

        fun description(description: String?) = apply { this.description = description }

        fun showDependency(showDependency: Dependency?) = apply { this.showDependency = showDependency }

        fun enableDependency(enableDependency: Dependency?) = apply { this.enableDependency = enableDependency }

        fun build() =
            ElementConfig(
                key = key,
                displayName = displayName,
                description = description,
                showDependency = showDependency,
                enableDependency = enableDependency,
            )
    }
}
