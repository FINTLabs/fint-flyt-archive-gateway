package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UrlBuilder(
    @field:NotBlank
    val urlTemplate: String? = null,
    val valueRefPerPathParamKey: Map<String, @NotBlank String>? = null,
    val valueRefPerRequestParamKey: Map<String, @NotBlank String>? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var urlTemplate: String? = null
        private var valueRefPerPathParamKey: Map<String, String>? = null
        private var valueRefPerRequestParamKey: Map<String, String>? = null

        fun urlTemplate(urlTemplate: String?) = apply { this.urlTemplate = urlTemplate }

        fun valueRefPerPathParamKey(valueRefPerPathParamKey: Map<String, String>?) =
            apply {
                this.valueRefPerPathParamKey = valueRefPerPathParamKey
            }

        fun valueRefPerRequestParamKey(valueRefPerRequestParamKey: Map<String, String>?) =
            apply {
                this.valueRefPerRequestParamKey = valueRefPerRequestParamKey
            }

        fun build() =
            UrlBuilder(
                urlTemplate = urlTemplate,
                valueRefPerPathParamKey = valueRefPerPathParamKey,
                valueRefPerRequestParamKey = valueRefPerRequestParamKey,
            )
    }
}
