package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValuePredicate(
    @field:NotBlank
    @field:Pattern(regexp = "[^.]*")
    val key: String? = null,
    val defined: Boolean? = null,
    val value: String? = null,
    val notValue: String? = null,
)
