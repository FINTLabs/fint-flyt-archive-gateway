package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class JournalpostDto(
    val tittel: String? = null,
    val offentligTittel: String? = null,
    val journalposttype: String? = null,
    val avskrivning: @Valid AvskrivningDto? = null,
    val administrativEnhet: String? = null,
    val saksbehandler: String? = null,
    val journalstatus: String? = null,
    val dokumentetsDato: String? = null,
    val tilgangsgruppe: String? = null,
    val skjerming: @Valid SkjermingDto? = null,
    val korrespondansepart: Collection<
        @NotNull @Valid
        KorrespondansepartDto,
    >? = null,
    val dokumentbeskrivelse: Collection<
        @NotNull @Valid
        DokumentbeskrivelseDto,
    >? = null,
)
