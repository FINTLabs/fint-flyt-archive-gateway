package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.util.Optional
import kotlin.jvm.JvmName

data class SakDto(
    @get:JvmName("getTittelOrNull")
    val tittel: String? = null,
    @get:JvmName("getOffentligTittelOrNull")
    val offentligTittel: String? = null,
    @get:JvmName("getSaksmappetypeOrNull")
    val saksmappetype: String? = null,
    @get:JvmName("getJournalenhetOrNull")
    val journalenhet: String? = null,
    @get:JvmName("getAdministrativEnhetOrNull")
    val administrativEnhet: String? = null,
    @get:JvmName("getSaksansvarligOrNull")
    val saksansvarlig: String? = null,
    @get:JvmName("getArkivdelOrNull")
    val arkivdel: String? = null,
    @get:JvmName("getSaksstatusOrNull")
    val saksstatus: String? = null,
    @get:JvmName("getTilgangsgruppeOrNull")
    val tilgangsgruppe: String? = null,
    @get:JvmName("getPartOrNull")
    val part: List<
        @NotNull @Valid
        PartDto,
    >? = null,
    @get:JvmName("getSkjermingOrNull")
    val skjerming: @Valid SkjermingDto? = null,
    @get:JvmName("getKlasseOrNull")
    val klasse: List<
        @NotNull @Valid
        KlasseDto,
    >? = null,
    @get:JvmName("getJournalpostOrNull")
    val journalpost: List<
        @NotNull @Valid
        JournalpostDto,
    >? = null,
) {
    fun getTittel(): Optional<String> = Optional.ofNullable(tittel)

    fun getOffentligTittel(): Optional<String> = Optional.ofNullable(offentligTittel)

    fun getSaksmappetype(): Optional<String> = Optional.ofNullable(saksmappetype)

    fun getSaksstatus(): Optional<String> = Optional.ofNullable(saksstatus)

    fun getTilgangsgruppe(): Optional<String> = Optional.ofNullable(tilgangsgruppe)

    fun getJournalenhet(): Optional<String> = Optional.ofNullable(journalenhet)

    fun getAdministrativEnhet(): Optional<String> = Optional.ofNullable(administrativEnhet)

    fun getSaksansvarlig(): Optional<String> = Optional.ofNullable(saksansvarlig)

    fun getArkivdel(): Optional<String> = Optional.ofNullable(arkivdel)

    fun getSkjerming(): Optional<SkjermingDto> = Optional.ofNullable(skjerming)

    fun getKlasse(): Optional<List<KlasseDto>> = Optional.ofNullable(klasse)

    fun getPart(): Optional<List<PartDto>> = Optional.ofNullable(part)

    fun getJournalpost(): Optional<List<JournalpostDto>> = Optional.ofNullable(journalpost)

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
