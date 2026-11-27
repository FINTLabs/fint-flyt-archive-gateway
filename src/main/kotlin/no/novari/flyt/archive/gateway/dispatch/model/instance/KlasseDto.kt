package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class KlasseDto(
    @field:NotNull
    val rekkefolge: Int? = null,
    val klassifikasjonssystem: String? = null,
    val klasseId: String? = null,
    val tittel: String? = null,
    val skjerming: @Valid SkjermingDto? = null,
)
