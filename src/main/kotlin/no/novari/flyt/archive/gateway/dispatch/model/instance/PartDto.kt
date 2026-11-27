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
)
