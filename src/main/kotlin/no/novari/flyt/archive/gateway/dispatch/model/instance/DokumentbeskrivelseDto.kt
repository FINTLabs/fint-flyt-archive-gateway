package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.util.Optional
import kotlin.jvm.JvmName

data class DokumentbeskrivelseDto(
    @get:JvmName("getTittelOrNull")
    val tittel: String? = null,
    @get:JvmName("getDokumentstatusOrNull")
    val dokumentstatus: String? = null,
    @get:JvmName("getDokumentTypeOrNull")
    val dokumentType: String? = null,
    @get:JvmName("getTilknyttetRegistreringSomOrNull")
    val tilknyttetRegistreringSom: String? = null,
    @get:JvmName("getDokumentobjektOrNull")
    val dokumentobjekt: Collection<
        @NotNull @Valid
        DokumentobjektDto,
    >? = null,
    @get:JvmName("getSkjermingOrNull")
    val skjerming: @Valid SkjermingDto? = null,
) {
    fun getTittel(): Optional<String> = Optional.ofNullable(tittel)

    fun getDokumentType(): Optional<String> = Optional.ofNullable(dokumentType)

    fun getTilknyttetRegistreringSom(): Optional<String> = Optional.ofNullable(tilknyttetRegistreringSom)

    fun getDokumentstatus(): Optional<String> = Optional.ofNullable(dokumentstatus)

    fun getDokumentobjekt(): Optional<Collection<DokumentobjektDto>> = Optional.ofNullable(dokumentobjekt)

    fun getSkjerming(): Optional<SkjermingDto> = Optional.ofNullable(skjerming)

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var tittel: String? = null
        private var dokumentstatus: String? = null
        private var dokumentType: String? = null
        private var tilknyttetRegistreringSom: String? = null
        private var dokumentobjekt: Collection<DokumentobjektDto>? = null
        private var skjerming: SkjermingDto? = null

        fun tittel(tittel: String?) = apply { this.tittel = tittel }

        fun dokumentstatus(dokumentstatus: String?) = apply { this.dokumentstatus = dokumentstatus }

        fun dokumentType(dokumentType: String?) = apply { this.dokumentType = dokumentType }

        fun tilknyttetRegistreringSom(tilknyttetRegistreringSom: String?) =
            apply {
                this.tilknyttetRegistreringSom = tilknyttetRegistreringSom
            }

        fun dokumentobjekt(dokumentobjekt: Collection<DokumentobjektDto>?) =
            apply {
                this.dokumentobjekt = dokumentobjekt
            }

        fun skjerming(skjerming: SkjermingDto?) = apply { this.skjerming = skjerming }

        fun build() =
            DokumentbeskrivelseDto(
                tittel = tittel,
                dokumentstatus = dokumentstatus,
                dokumentType = dokumentType,
                tilknyttetRegistreringSom = tilknyttetRegistreringSom,
                dokumentobjekt = dokumentobjekt,
                skjerming = skjerming,
            )
    }
}
