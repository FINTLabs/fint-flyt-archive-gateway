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
}
