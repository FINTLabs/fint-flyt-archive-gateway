package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class SakDto(
    val tittel: String? = null,
    val offentligTittel: String? = null,
    val saksmappetype: String? = null,
    val journalenhet: String? = null,
    val administrativEnhet: String? = null,
    val saksansvarlig: String? = null,
    val arkivdel: String? = null,
    val saksstatus: String? = null,
    val tilgangsgruppe: String? = null,
    val part: List<
        @NotNull @Valid
        PartDto,
    >? = null,
    val skjerming: @Valid SkjermingDto? = null,
    val klasse: List<
        @NotNull @Valid
        KlasseDto,
    >? = null,
    val journalpost: List<
        @NotNull @Valid
        JournalpostDto,
    >? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var tittel: String? = null
        private var offentligTittel: String? = null
        private var saksmappetype: String? = null
        private var journalenhet: String? = null
        private var administrativEnhet: String? = null
        private var saksansvarlig: String? = null
        private var arkivdel: String? = null
        private var saksstatus: String? = null
        private var tilgangsgruppe: String? = null
        private var part: List<PartDto>? = null
        private var skjerming: SkjermingDto? = null
        private var klasse: List<KlasseDto>? = null
        private var journalpost: List<JournalpostDto>? = null

        fun tittel(tittel: String?) = apply { this.tittel = tittel }

        fun offentligTittel(offentligTittel: String?) = apply { this.offentligTittel = offentligTittel }

        fun saksmappetype(saksmappetype: String?) = apply { this.saksmappetype = saksmappetype }

        fun journalenhet(journalenhet: String?) = apply { this.journalenhet = journalenhet }

        fun administrativEnhet(administrativEnhet: String?) = apply { this.administrativEnhet = administrativEnhet }

        fun saksansvarlig(saksansvarlig: String?) = apply { this.saksansvarlig = saksansvarlig }

        fun arkivdel(arkivdel: String?) = apply { this.arkivdel = arkivdel }

        fun saksstatus(saksstatus: String?) = apply { this.saksstatus = saksstatus }

        fun tilgangsgruppe(tilgangsgruppe: String?) = apply { this.tilgangsgruppe = tilgangsgruppe }

        fun part(part: List<PartDto>?) = apply { this.part = part }

        fun skjerming(skjerming: SkjermingDto?) = apply { this.skjerming = skjerming }

        fun klasse(klasse: List<KlasseDto>?) = apply { this.klasse = klasse }

        fun journalpost(journalpost: List<JournalpostDto>?) = apply { this.journalpost = journalpost }

        fun build() =
            SakDto(
                tittel = tittel,
                offentligTittel = offentligTittel,
                saksmappetype = saksmappetype,
                journalenhet = journalenhet,
                administrativEnhet = administrativEnhet,
                saksansvarlig = saksansvarlig,
                arkivdel = arkivdel,
                saksstatus = saksstatus,
                tilgangsgruppe = tilgangsgruppe,
                part = part,
                skjerming = skjerming,
                klasse = klasse,
                journalpost = journalpost,
            )
    }
}
