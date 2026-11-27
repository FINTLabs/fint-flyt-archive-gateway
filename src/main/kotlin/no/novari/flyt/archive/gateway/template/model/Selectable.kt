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
)
