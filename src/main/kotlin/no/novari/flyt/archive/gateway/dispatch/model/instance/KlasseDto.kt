package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.util.Optional
import kotlin.jvm.JvmName

data class KlasseDto(
    @field:NotNull
    @get:JvmName("getRekkefolgeOrNull")
    val rekkefolge: Int? = null,
    @get:JvmName("getKlassifikasjonssystemOrNull")
    val klassifikasjonssystem: String? = null,
    @get:JvmName("getKlasseIdOrNull")
    val klasseId: String? = null,
    @get:JvmName("getTittelOrNull")
    val tittel: String? = null,
    @get:JvmName("getSkjermingOrNull")
    val skjerming: @Valid SkjermingDto? = null,
) {
    fun getRekkefolge(): Optional<Int> = Optional.ofNullable(rekkefolge)

    fun getKlassifikasjonssystem(): Optional<String> = Optional.ofNullable(klassifikasjonssystem)

    fun getKlasseId(): Optional<String> = Optional.ofNullable(klasseId)

    fun getSkjerming(): Optional<SkjermingDto> = Optional.ofNullable(skjerming)

    fun getTittel(): Optional<String> = Optional.ofNullable(tittel)

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
