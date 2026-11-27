package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UrlBuilder(
    @field:NotBlank
    val urlTemplate: String? = null,
    val valueRefPerPathParamKey: Map<String, @NotBlank String>? = null,
    val valueRefPerRequestParamKey: Map<String, @NotBlank String>? = null,
)
