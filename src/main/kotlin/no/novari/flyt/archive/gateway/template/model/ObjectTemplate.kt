package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import no.novari.flyt.archive.gateway.template.validation.UniqueKeys
import no.novari.flyt.archive.gateway.template.validation.UniqueOrders

@UniqueKeys
@UniqueOrders
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ObjectTemplate
    @JsonCreator
    constructor(
        @param:JsonProperty("valueTemplates")
        val valueTemplates: Collection<
            @NotNull @Valid
            ElementTemplate<ValueTemplate>,
        > = emptyList(),
        @param:JsonProperty("selectableValueTemplates")
        val selectableValueTemplates: Collection<
            @NotNull @Valid
            ElementTemplate<SelectableValueTemplate>,
        > =
            emptyList(),
        @param:JsonProperty("valueCollectionTemplates")
        val valueCollectionTemplates: Collection<
            @NotNull @Valid
            ElementTemplate<CollectionTemplate<ValueTemplate>>,
        > =
            emptyList(),
        @param:JsonProperty("objectTemplates")
        val objectTemplates: Collection<
            @NotNull @Valid
            ElementTemplate<ObjectTemplate>,
        > = emptyList(),
        @param:JsonProperty("objectCollectionTemplates")
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
