package no.novari.flyt.archive.gateway.template.model

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValueTemplate(
    @field:NotNull
    val type: Type? = null,
    @field:Valid
    val search: UrlBuilder? = null,
) {
    enum class Type {
        STRING,
        DYNAMIC_STRING,
        FILE,
        BOOLEAN,
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var type: Type? = null
        private var search: UrlBuilder? = null

        fun type(type: Type?) = apply { this.type = type }

        fun search(search: UrlBuilder?) = apply { this.search = search }

        fun build() = ValueTemplate(type = type, search = search)
    }
}
