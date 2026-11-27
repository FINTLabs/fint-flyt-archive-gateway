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
    val referenceTemplate: ReferenceTemplate? = null,
) {
    enum class Type {
        DYNAMIC_STRING_OR_SEARCH_SELECT,
        SEARCH_SELECT,
        DROPDOWN,
    }
}
