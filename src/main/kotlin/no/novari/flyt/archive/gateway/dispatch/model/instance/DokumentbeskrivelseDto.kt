package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class DokumentbeskrivelseDto(
    val tittel: String? = null,
    val dokumentstatus: String? = null,
    val dokumentType: String? = null,
    val tilknyttetRegistreringSom: String? = null,
    val dokumentobjekt: Collection<
        @NotNull @Valid
        DokumentobjektDto,
    >? = null,
    val skjerming: @Valid SkjermingDto? = null,
) {
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
