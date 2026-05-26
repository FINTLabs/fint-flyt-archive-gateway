package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.SkjermingResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.KlasseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class KlasseMappingServiceTest {
    private lateinit var mockSkjermingMappingService: SkjermingMappingService
    private lateinit var klasseMappingService: KlasseMappingService

    @BeforeEach
    fun setup() {
        mockSkjermingMappingService = mock()
        klasseMappingService = KlasseMappingService(mockSkjermingMappingService)
    }

    @Test
    fun `given a null input, returns null`() {
        assertThat(klasseMappingService.toKlasse(null)).isNull()
    }

    @Test
    fun `given a valid input, maps to KlasseResources`() {
        val skjermingDto =
            SkjermingDto
                .builder()
                .tilgangsrestriksjon("testTilgangsrestriksjon")
                .skjermingshjemmel("testSkjermingshjemmel")
                .build()
        val dto =
            KlasseDto
                .builder()
                .klasseId("testId")
                .skjerming(skjermingDto)
                .tittel("testTitle")
                .klassifikasjonssystem("testKlassifikasjonssystem")
                .rekkefolge(1)
                .build()

        doReturn(SkjermingResource()).whenever(mockSkjermingMappingService).toSkjermingResource(any())

        val klasseResources = klasseMappingService.toKlasse(listOf(dto))

        assertThat(klasseResources).hasSize(1)
        assertThat(klasseResources!!.first().klasseId).isEqualTo("testId")
        assertThat(klasseResources.first().tittel).isEqualTo("testTitle")
        assertThat(klasseResources.first().klassifikasjonssystem).contains(Link.with("testKlassifikasjonssystem"))
        assertThat(klasseResources.first().rekkefolge).isEqualTo(1)
    }
}
