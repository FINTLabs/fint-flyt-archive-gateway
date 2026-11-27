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
)
