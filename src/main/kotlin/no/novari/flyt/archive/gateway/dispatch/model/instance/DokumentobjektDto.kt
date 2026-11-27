package no.novari.flyt.archive.gateway.dispatch.model.instance

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class DokumentobjektDto(
    val variantformat: String? = null,
    val filformat: String? = null,
    val format: String? = null,
    @field:JsonProperty("fil")
    @field:NotNull
    val fileId: UUID? = null,
)
