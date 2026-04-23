package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import java.util.Optional
import kotlin.jvm.JvmName

data class PartDto(
    @get:JvmName("getPartNavnOrNull")
    val partNavn: String? = null,
    @get:JvmName("getPartRolleOrNull")
    val partRolle: String? = null,
    @get:JvmName("getKontaktpersonOrNull")
    val kontaktperson: String? = null,
    @get:JvmName("getOrganisasjonsnummerOrNull")
    val organisasjonsnummer: String? = null,
    @get:JvmName("getFodselsnummerOrNull")
    val fodselsnummer: String? = null,
    @get:JvmName("getAdresseOrNull")
    val adresse: @Valid AdresseDto? = null,
    @get:JvmName("getKontaktinformasjonOrNull")
    val kontaktinformasjon: @Valid KontaktinformasjonDto? = null,
) {
    fun getPartNavn(): Optional<String> = Optional.ofNullable(partNavn)

    fun getPartRolle(): Optional<String> = Optional.ofNullable(partRolle)

    fun getKontaktperson(): Optional<String> = Optional.ofNullable(kontaktperson)

    fun getOrganisasjonsnummer(): Optional<String> = Optional.ofNullable(organisasjonsnummer)

    fun getFodselsnummer(): Optional<String> = Optional.ofNullable(fodselsnummer)

    fun getAdresse(): Optional<AdresseDto> = Optional.ofNullable(adresse)

    fun getKontaktinformasjon(): Optional<KontaktinformasjonDto> = Optional.ofNullable(kontaktinformasjon)

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
