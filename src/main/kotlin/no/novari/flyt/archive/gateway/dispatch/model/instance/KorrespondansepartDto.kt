package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid

data class KorrespondansepartDto(
    val korrespondanseparttype: String? = null,
    val organisasjonsnummer: String? = null,
    val fodselsnummer: String? = null,
    val korrespondansepartNavn: String? = null,
    val kontaktperson: String? = null,
    val adresse: @Valid AdresseDto? = null,
    val kontaktinformasjon: @Valid KontaktinformasjonDto? = null,
    val skjerming: @Valid SkjermingDto? = null,
) {
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
