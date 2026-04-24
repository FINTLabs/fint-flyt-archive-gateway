package no.novari.flyt.archive.gateway.template.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class MappingTemplate(
    val displayName: String? = null,
    @field:NotNull
    @field:Valid
    val rootObjectTemplate: ObjectTemplate? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var displayName: String? = null
        private var rootObjectTemplate: ObjectTemplate? = null

        fun displayName(displayName: String?) = apply { this.displayName = displayName }

        fun rootObjectTemplate(rootObjectTemplate: ObjectTemplate?) =
            apply {
                this.rootObjectTemplate = rootObjectTemplate
            }

        fun build() = MappingTemplate(displayName = displayName, rootObjectTemplate = rootObjectTemplate)
    }
}
