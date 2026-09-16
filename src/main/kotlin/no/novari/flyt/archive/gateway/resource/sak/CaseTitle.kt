package no.novari.flyt.archive.gateway.resource.sak

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Tittel for en arkivsak.")
data class CaseTitle(
    @field:Schema(description = "Sakstittel fra arkivet.", example = "Innsynsbegjæring 2024")
    val value: String,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var value: String? = null

        fun value(value: String) = apply { this.value = value }

        fun build() = CaseTitle(requireNotNull(value) { "value is required" })
    }
}
