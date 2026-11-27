package no.novari.flyt.archive.gateway.template.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class MappingTemplate(
    val displayName: String? = null,
    @field:NotNull
    @field:Valid
    val rootObjectTemplate: ObjectTemplate? = null,
)
