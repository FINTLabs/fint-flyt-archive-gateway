package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class SakDto(
    val tittel: String? = null,
    val offentligTittel: String? = null,
    val saksmappetype: String? = null,
    val journalenhet: String? = null,
    val administrativEnhet: String? = null,
    val saksansvarlig: String? = null,
    val arkivdel: String? = null,
    val saksstatus: String? = null,
    val tilgangsgruppe: String? = null,
    val part: List<
        @NotNull @Valid
        PartDto,
    >? = null,
    val skjerming: @Valid SkjermingDto? = null,
    val klasse: List<
        @NotNull @Valid
        KlasseDto,
    >? = null,
    val journalpost: List<
        @NotNull @Valid
        JournalpostDto,
    >? = null,
)
