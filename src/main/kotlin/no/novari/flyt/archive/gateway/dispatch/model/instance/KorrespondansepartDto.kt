package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import java.util.Optional
import kotlin.jvm.JvmName

data class KorrespondansepartDto(
    @get:JvmName("getKorrespondanseparttypeOrNull")
    val korrespondanseparttype: String? = null,
    @get:JvmName("getOrganisasjonsnummerOrNull")
    val organisasjonsnummer: String? = null,
    @get:JvmName("getFodselsnummerOrNull")
    val fodselsnummer: String? = null,
    @get:JvmName("getKorrespondansepartNavnOrNull")
    val korrespondansepartNavn: String? = null,
    @get:JvmName("getKontaktpersonOrNull")
    val kontaktperson: String? = null,
    @get:JvmName("getAdresseOrNull")
    val adresse: @Valid AdresseDto? = null,
    @get:JvmName("getKontaktinformasjonOrNull")
    val kontaktinformasjon: @Valid KontaktinformasjonDto? = null,
    @get:JvmName("getSkjermingOrNull")
    val skjerming: @Valid SkjermingDto? = null,
) {
    fun getKorrespondanseparttype(): Optional<String> = Optional.ofNullable(korrespondanseparttype)

    fun getFodselsnummer(): Optional<String> = Optional.ofNullable(fodselsnummer)

    fun getOrganisasjonsnummer(): Optional<String> = Optional.ofNullable(organisasjonsnummer)

    fun getKorrespondansepartNavn(): Optional<String> = Optional.ofNullable(korrespondansepartNavn)

    fun getAdresse(): Optional<AdresseDto> = Optional.ofNullable(adresse)

    fun getKontaktperson(): Optional<String> = Optional.ofNullable(kontaktperson)

    fun getKontaktinformasjon(): Optional<KontaktinformasjonDto> = Optional.ofNullable(kontaktinformasjon)

    fun getSkjerming(): Optional<SkjermingDto> = Optional.ofNullable(skjerming)

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var korrespondanseparttype: String? = null
        private var organisasjonsnummer: String? = null
        private var fodselsnummer: String? = null
        private var korrespondansepartNavn: String? = null
        private var kontaktperson: String? = null
        private var adresse: AdresseDto? = null
        private var kontaktinformasjon: KontaktinformasjonDto? = null
        private var skjerming: SkjermingDto? = null

        fun korrespondanseparttype(korrespondanseparttype: String?) =
            apply {
                this.korrespondanseparttype = korrespondanseparttype
            }

        fun organisasjonsnummer(organisasjonsnummer: String?) =
            apply {
                this.organisasjonsnummer = organisasjonsnummer
            }

        fun fodselsnummer(fodselsnummer: String?) = apply { this.fodselsnummer = fodselsnummer }

        fun korrespondansepartNavn(korrespondansepartNavn: String?) =
            apply {
                this.korrespondansepartNavn = korrespondansepartNavn
            }

        fun kontaktperson(kontaktperson: String?) = apply { this.kontaktperson = kontaktperson }

        fun adresse(adresse: AdresseDto?) = apply { this.adresse = adresse }

        fun kontaktinformasjon(kontaktinformasjon: KontaktinformasjonDto?) =
            apply {
                this.kontaktinformasjon = kontaktinformasjon
            }

        fun skjerming(skjerming: SkjermingDto?) = apply { this.skjerming = skjerming }

        fun build() =
            KorrespondansepartDto(
                korrespondanseparttype = korrespondanseparttype,
                organisasjonsnummer = organisasjonsnummer,
                fodselsnummer = fodselsnummer,
                korrespondansepartNavn = korrespondansepartNavn,
                kontaktperson = kontaktperson,
                adresse = adresse,
                kontaktinformasjon = kontaktinformasjon,
                skjerming = skjerming,
            )
    }
}
