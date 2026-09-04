package no.novari.flyt.archive.gateway.resource.kodeverk

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResourceReference(
    val id: String,
    val displayName: String,
    val functionalId: String? = null,
    val name: String? = null,
    val technicalId: String? = null,
)
