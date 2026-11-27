package no.novari.flyt.archive.gateway.dispatch.model.instance

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class DokumentbeskrivelseDto(
    val tittel: String? = null,
    val dokumentstatus: String? = null,
    val dokumentType: String? = null,
    val tilknyttetRegistreringSom: String? = null,
    val dokumentobjekt: Collection<
        @NotNull @Valid
        DokumentobjektDto,
    >? = null,
    val skjerming: @Valid SkjermingDto? = null,
)
