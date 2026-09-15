package no.novari.flyt.archive.gateway.resource.kodeverk

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Referanse til en arkivressurs eller et kodeverksobjekt.")
data class ResourceReference(
    @field:Schema(description = "Self-link eller entydig identifikator for ressursen.")
    val id: String,
    @field:Schema(description = "Visningsnavn satt sammen av funksjonell id, navn og teknisk id.")
    val displayName: String,
    @field:Schema(description = "Funksjonell id, for eksempel kode eller brukernavn.", nullable = true)
    val functionalId: String? = null,
    @field:Schema(description = "Lesbart navn på ressursen.", nullable = true)
    val name: String? = null,
    @field:Schema(description = "Teknisk system-id fra arkivet.", nullable = true)
    val technicalId: String? = null,
)
