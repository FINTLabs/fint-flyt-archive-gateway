package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import no.novari.flyt.archive.gateway.template.validation.UniqueKeys
import no.novari.flyt.archive.gateway.template.validation.UniqueOrders

@UniqueKeys
@UniqueOrders
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ObjectTemplate(
    val valueTemplates: Collection<
        @NotNull @Valid
        ElementTemplate<ValueTemplate>,
    > = emptyList(),
    val selectableValueTemplates: Collection<
        @NotNull @Valid
        ElementTemplate<SelectableValueTemplate>,
    > = emptyList(),
    val valueCollectionTemplates: Collection<
        @NotNull @Valid
        ElementTemplate<CollectionTemplate<ValueTemplate>>,
    > = emptyList(),
    val objectTemplates: Collection<
        @NotNull @Valid
        ElementTemplate<ObjectTemplate>,
    > = emptyList(),
    val objectCollectionTemplates: Collection<
        @NotNull @Valid
        ElementTemplate<CollectionTemplate<ObjectTemplate>>,
    > = emptyList(),
) {
    companion object {
        @JvmStatic
        fun builder() = ObjectTemplateBuilder()
    }
}
