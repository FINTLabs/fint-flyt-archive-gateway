package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.constraints.NotNull

data class AdresseDto(
    val adresselinje: Collection<@NotNull String>? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
)
