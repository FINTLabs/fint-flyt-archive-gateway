package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.flyt.archive.gateway.dispatch.model.instance.DokumentbeskrivelseDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DokumentbeskrivelseMappingServiceTest {
    private val dokumentbeskrivelseMappingService =
        DokumentbeskrivelseMappingService(
            DokumentObjektMappingService(),
            SkjermingMappingService(),
        )

    @Test
    fun `maps to DokumentbeskrivelseResource`() {
        val dto =
            DokumentbeskrivelseDto
                .builder()
                .tittel("testTittel")
                .dokumentstatus("testDokumentstatus")
                .dokumentType("testDokumenttype")
                .tilknyttetRegistreringSom("testTilknyttetRegistreringSom")
                .build()

        val resource = dokumentbeskrivelseMappingService.toDokumentbeskrivelseResource(dto, emptyMap())

        assertThat(resource.tittel).isEqualTo("testTittel")
        assertThat(resource.dokumentType).isEqualTo(listOf(Link.with("testDokumenttype")))
        assertThat(resource.dokumentstatus).isEqualTo(listOf(Link.with("testDokumentstatus")))
        assertThat(resource.tilknyttetRegistreringSom).isEqualTo(listOf(Link.with("testTilknyttetRegistreringSom")))
    }
}
