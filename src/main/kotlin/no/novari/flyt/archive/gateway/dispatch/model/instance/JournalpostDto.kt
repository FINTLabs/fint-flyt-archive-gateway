package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class JournalpostDto(
    val tittel: String? = null,
    val offentligTittel: String? = null,
    val journalposttype: String? = null,
    val avskrivning: @Valid AvskrivningDto? = null,
    val administrativEnhet: String? = null,
    val saksbehandler: String? = null,
    val journalstatus: String? = null,
    val dokumentetsDato: String? = null,
    val tilgangsgruppe: String? = null,
    val skjerming: @Valid SkjermingDto? = null,
    val korrespondansepart: Collection<
        @NotNull @Valid
        KorrespondansepartDto,
    >? = null,
    val dokumentbeskrivelse: Collection<
        @NotNull @Valid
        DokumentbeskrivelseDto,
    >? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var tittel: String? = null
        private var offentligTittel: String? = null
        private var journalposttype: String? = null
        private var avskrivning: AvskrivningDto? = null
        private var administrativEnhet: String? = null
        private var saksbehandler: String? = null
        private var journalstatus: String? = null
        private var dokumentetsDato: String? = null
        private var tilgangsgruppe: String? = null
        private var skjerming: SkjermingDto? = null
        private var korrespondansepart: Collection<KorrespondansepartDto>? = null
        private var dokumentbeskrivelse: Collection<DokumentbeskrivelseDto>? = null

        fun tittel(tittel: String?) = apply { this.tittel = tittel }

        fun offentligTittel(offentligTittel: String?) = apply { this.offentligTittel = offentligTittel }

        fun journalposttype(journalposttype: String?) = apply { this.journalposttype = journalposttype }

        fun avskrivning(avskrivning: AvskrivningDto?) = apply { this.avskrivning = avskrivning }

        fun administrativEnhet(administrativEnhet: String?) = apply { this.administrativEnhet = administrativEnhet }

        fun saksbehandler(saksbehandler: String?) = apply { this.saksbehandler = saksbehandler }

        fun journalstatus(journalstatus: String?) = apply { this.journalstatus = journalstatus }

        fun dokumentetsDato(dokumentetsDato: String?) = apply { this.dokumentetsDato = dokumentetsDato }

        fun tilgangsgruppe(tilgangsgruppe: String?) = apply { this.tilgangsgruppe = tilgangsgruppe }

        fun skjerming(skjerming: SkjermingDto?) = apply { this.skjerming = skjerming }

        fun korrespondansepart(korrespondansepart: Collection<KorrespondansepartDto>?) =
            apply {
                this.korrespondansepart = korrespondansepart
            }

        fun dokumentbeskrivelse(dokumentbeskrivelse: Collection<DokumentbeskrivelseDto>?) =
            apply {
                this.dokumentbeskrivelse = dokumentbeskrivelse
            }

        fun build() =
            JournalpostDto(
                tittel = tittel,
                offentligTittel = offentligTittel,
                journalposttype = journalposttype,
                avskrivning = avskrivning,
                administrativEnhet = administrativEnhet,
                saksbehandler = saksbehandler,
                journalstatus = journalstatus,
                dokumentetsDato = dokumentetsDato,
                tilgangsgruppe = tilgangsgruppe,
                skjerming = skjerming,
                korrespondansepart = korrespondansepart,
                dokumentbeskrivelse = dokumentbeskrivelse,
            )
    }
}
