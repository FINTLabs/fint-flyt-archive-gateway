package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.util.Optional
import kotlin.jvm.JvmName

data class JournalpostDto(
    @get:JvmName("getTittelOrNull")
    val tittel: String? = null,
    @get:JvmName("getOffentligTittelOrNull")
    val offentligTittel: String? = null,
    @get:JvmName("getJournalposttypeOrNull")
    val journalposttype: String? = null,
    @get:JvmName("getAdministrativEnhetOrNull")
    val administrativEnhet: String? = null,
    @get:JvmName("getSaksbehandlerOrNull")
    val saksbehandler: String? = null,
    @get:JvmName("getJournalstatusOrNull")
    val journalstatus: String? = null,
    @get:JvmName("getTilgangsgruppeOrNull")
    val tilgangsgruppe: String? = null,
    @get:JvmName("getSkjermingOrNull")
    val skjerming: @Valid SkjermingDto? = null,
    @get:JvmName("getKorrespondansepartOrNull")
    val korrespondansepart: Collection<
        @NotNull @Valid
        KorrespondansepartDto,
    >? = null,
    @get:JvmName("getDokumentbeskrivelseOrNull")
    val dokumentbeskrivelse: Collection<
        @NotNull @Valid
        DokumentbeskrivelseDto,
    >? = null,
) {
    fun getTittel(): Optional<String> = Optional.ofNullable(tittel)

    fun getOffentligTittel(): Optional<String> = Optional.ofNullable(offentligTittel)

    fun getJournalstatus(): Optional<String> = Optional.ofNullable(journalstatus)

    fun getTilgangsgruppe(): Optional<String> = Optional.ofNullable(tilgangsgruppe)

    fun getSaksbehandler(): Optional<String> = Optional.ofNullable(saksbehandler)

    fun getJournalposttype(): Optional<String> = Optional.ofNullable(journalposttype)

    fun getAdministrativEnhet(): Optional<String> = Optional.ofNullable(administrativEnhet)

    fun getSkjerming(): Optional<SkjermingDto> = Optional.ofNullable(skjerming)

    fun getKorrespondansepart(): Optional<Collection<KorrespondansepartDto>> = Optional.ofNullable(korrespondansepart)

    fun getDokumentbeskrivelse(): Optional<Collection<DokumentbeskrivelseDto>> =
        Optional.ofNullable(dokumentbeskrivelse)

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var tittel: String? = null
        private var offentligTittel: String? = null
        private var journalposttype: String? = null
        private var administrativEnhet: String? = null
        private var saksbehandler: String? = null
        private var journalstatus: String? = null
        private var tilgangsgruppe: String? = null
        private var skjerming: SkjermingDto? = null
        private var korrespondansepart: Collection<KorrespondansepartDto>? = null
        private var dokumentbeskrivelse: Collection<DokumentbeskrivelseDto>? = null

        fun tittel(tittel: String?) = apply { this.tittel = tittel }

        fun offentligTittel(offentligTittel: String?) = apply { this.offentligTittel = offentligTittel }

        fun journalposttype(journalposttype: String?) = apply { this.journalposttype = journalposttype }

        fun administrativEnhet(administrativEnhet: String?) = apply { this.administrativEnhet = administrativEnhet }

        fun saksbehandler(saksbehandler: String?) = apply { this.saksbehandler = saksbehandler }

        fun journalstatus(journalstatus: String?) = apply { this.journalstatus = journalstatus }

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
                administrativEnhet = administrativEnhet,
                saksbehandler = saksbehandler,
                journalstatus = journalstatus,
                tilgangsgruppe = tilgangsgruppe,
                skjerming = skjerming,
                korrespondansepart = korrespondansepart,
                dokumentbeskrivelse = dokumentbeskrivelse,
            )
    }
}
