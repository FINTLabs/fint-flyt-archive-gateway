package no.novari.flyt.archive.gateway.dispatch.model.instance

import java.util.Optional
import kotlin.jvm.JvmName

data class CaseSearchParametersDto(
    val arkivdel: Boolean = false,
    val administrativEnhet: Boolean = false,
    val tilgangsrestriksjon: Boolean = false,
    val saksmappetype: Boolean = false,
    val saksstatus: Boolean = false,
    val tittel: Boolean = false,
    val klassering: Boolean = false,
    @get:JvmName("getKlasseringRekkefolgeOrNull")
    val klasseringRekkefolge: String? = null,
    @get:JvmName("getKlasseringKlassifikasjonssystemOrNull")
    val klasseringKlassifikasjonssystem: Boolean? = null,
    @get:JvmName("getKlasseringKlasseIdOrNull")
    val klasseringKlasseId: Boolean? = null,
) {
    fun getKlasseringRekkefolge(): Optional<String> = Optional.ofNullable(klasseringRekkefolge)

    fun getKlasseringKlassifikasjonssystem(): Boolean = klasseringKlassifikasjonssystem ?: false

    fun getKlasseringKlasseId(): Boolean = klasseringKlasseId ?: false

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var arkivdel: Boolean = false
        private var administrativEnhet: Boolean = false
        private var tilgangsrestriksjon: Boolean = false
        private var saksmappetype: Boolean = false
        private var saksstatus: Boolean = false
        private var tittel: Boolean = false
        private var klassering: Boolean = false
        private var klasseringRekkefolge: String? = null
        private var klasseringKlassifikasjonssystem: Boolean? = null
        private var klasseringKlasseId: Boolean? = null

        fun arkivdel(arkivdel: Boolean) = apply { this.arkivdel = arkivdel }

        fun administrativEnhet(administrativEnhet: Boolean) = apply { this.administrativEnhet = administrativEnhet }

        fun tilgangsrestriksjon(tilgangsrestriksjon: Boolean) = apply { this.tilgangsrestriksjon = tilgangsrestriksjon }

        fun saksmappetype(saksmappetype: Boolean) = apply { this.saksmappetype = saksmappetype }

        fun saksstatus(saksstatus: Boolean) = apply { this.saksstatus = saksstatus }

        fun tittel(tittel: Boolean) = apply { this.tittel = tittel }

        fun klassering(klassering: Boolean) = apply { this.klassering = klassering }

        fun klasseringRekkefolge(klasseringRekkefolge: String?) =
            apply {
                this.klasseringRekkefolge = klasseringRekkefolge
            }

        fun klasseringKlassifikasjonssystem(klasseringKlassifikasjonssystem: Boolean?) =
            apply {
                this.klasseringKlassifikasjonssystem = klasseringKlassifikasjonssystem
            }

        fun klasseringKlasseId(klasseringKlasseId: Boolean?) =
            apply {
                this.klasseringKlasseId = klasseringKlasseId
            }

        fun build() =
            CaseSearchParametersDto(
                arkivdel = arkivdel,
                administrativEnhet = administrativEnhet,
                tilgangsrestriksjon = tilgangsrestriksjon,
                saksmappetype = saksmappetype,
                saksstatus = saksstatus,
                tittel = tittel,
                klassering = klassering,
                klasseringRekkefolge = klasseringRekkefolge,
                klasseringKlassifikasjonssystem = klasseringKlassifikasjonssystem,
                klasseringKlasseId = klasseringKlasseId,
            )
    }
}
