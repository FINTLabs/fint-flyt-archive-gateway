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
)
