package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import no.novari.flyt.archive.gateway.template.validation.AtLeastOneSelectable

@AtLeastOneSelectable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SelectableValueTemplate(
    @field:NotNull
    val type: Type? = null,
    val selectables: Collection<@Valid Selectable>? = null,
    val selectablesSources: Collection<@Valid UrlBuilder>? = null,
) {
    enum class Type {
        DYNAMIC_STRING_OR_SEARCH_SELECT,
        SEARCH_SELECT,
        DROPDOWN,
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var type: Type? = null
        private var selectables: Collection<Selectable>? = null
        private var selectablesSources: Collection<UrlBuilder>? = null

        fun type(type: Type?) = apply { this.type = type }

        fun selectables(selectables: Collection<Selectable>?) = apply { this.selectables = selectables }

        fun selectablesSources(selectablesSources: Collection<UrlBuilder>?) =
            apply {
                this.selectablesSources = selectablesSources
            }

        fun build() =
            SelectableValueTemplate(
                type = type,
                selectables = selectables,
                selectablesSources = selectablesSources,
            )
    }
}
