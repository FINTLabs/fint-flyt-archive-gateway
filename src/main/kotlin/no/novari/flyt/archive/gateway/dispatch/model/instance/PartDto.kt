package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid

data class PartDto(
    val partNavn: String? = null,
    val partRolle: String? = null,
    val kontaktperson: String? = null,
    val organisasjonsnummer: String? = null,
    val fodselsnummer: String? = null,
    val adresse: @Valid AdresseDto? = null,
    val kontaktinformasjon: @Valid KontaktinformasjonDto? = null,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var partNavn: String? = null
        private var partRolle: String? = null
        private var kontaktperson: String? = null
        private var organisasjonsnummer: String? = null
        private var fodselsnummer: String? = null
        private var adresse: AdresseDto? = null
        private var kontaktinformasjon: KontaktinformasjonDto? = null

        fun partNavn(partNavn: String?) = apply { this.partNavn = partNavn }

        fun partRolle(partRolle: String?) = apply { this.partRolle = partRolle }

        fun kontaktperson(kontaktperson: String?) = apply { this.kontaktperson = kontaktperson }

        fun organisasjonsnummer(organisasjonsnummer: String?) =
            apply {
                this.organisasjonsnummer = organisasjonsnummer
            }

        fun fodselsnummer(fodselsnummer: String?) = apply { this.fodselsnummer = fodselsnummer }

        fun adresse(adresse: AdresseDto?) = apply { this.adresse = adresse }

        fun kontaktinformasjon(kontaktinformasjon: KontaktinformasjonDto?) =
            apply {
                this.kontaktinformasjon = kontaktinformasjon
            }

        fun build() =
            PartDto(
                partNavn = partNavn,
                partRolle = partRolle,
                kontaktperson = kontaktperson,
                organisasjonsnummer = organisasjonsnummer,
                fodselsnummer = fodselsnummer,
                adresse = adresse,
                kontaktinformasjon = kontaktinformasjon,
            )
    }
}
