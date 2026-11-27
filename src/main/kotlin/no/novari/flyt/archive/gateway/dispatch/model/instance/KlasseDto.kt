package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class KlasseDto(
    @field:NotNull
    val rekkefolge: Int? = null,
    val klassifikasjonssystem: String? = null,
    val klasseId: String? = null,
    val tittel: String? = null,
    val skjerming: @Valid SkjermingDto? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var rekkefolge: Int? = null
        private var klassifikasjonssystem: String? = null
        private var klasseId: String? = null
        private var tittel: String? = null
        private var skjerming: SkjermingDto? = null

        fun rekkefolge(rekkefolge: Int?) = apply { this.rekkefolge = rekkefolge }

        fun klassifikasjonssystem(klassifikasjonssystem: String?) =
            apply {
                this.klassifikasjonssystem = klassifikasjonssystem
            }

        fun klasseId(klasseId: String?) = apply { this.klasseId = klasseId }

        fun tittel(tittel: String?) = apply { this.tittel = tittel }

        fun skjerming(skjerming: SkjermingDto?) = apply { this.skjerming = skjerming }

        fun build() =
            KlasseDto(
                rekkefolge = rekkefolge,
                klassifikasjonssystem = klassifikasjonssystem,
                klasseId = klasseId,
                tittel = tittel,
                skjerming = skjerming,
            )
    }
}
