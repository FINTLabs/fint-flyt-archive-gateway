package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Dependency(
    val hasAnyCombination: Collection<Collection<@Valid ValuePredicate>>? = null,
)
