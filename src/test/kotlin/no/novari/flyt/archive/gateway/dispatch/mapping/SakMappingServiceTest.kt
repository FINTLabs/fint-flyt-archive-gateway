package no.novari.flyt.archive.gateway.dispatch.mapping

import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.KlasseResource
import no.novari.fint.model.resource.arkiv.noark.PartResource
import no.novari.fint.model.resource.arkiv.noark.SkjermingResource
import no.novari.flyt.archive.gateway.dispatch.model.instance.KlasseDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.PartDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SakDto
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class SakMappingServiceTest {
    private lateinit var skjermingMappingService: SkjermingMappingService
    private lateinit var klasseMappingService: KlasseMappingService
    private lateinit var partMappingService: PartMappingService
    private lateinit var sakMappingService: SakMappingService

    @BeforeEach
    fun setup() {
        skjermingMappingService = mock()
        klasseMappingService = mock()
        partMappingService = mock()
        sakMappingService = SakMappingService(skjermingMappingService, klasseMappingService, partMappingService)
    }

    @Test
    fun shouldMapToSakResource() {
        val skjermingDto: SkjermingDto = mock()
        val skjermingResource: SkjermingResource = mock()
        whenever(skjermingMappingService.toSkjermingResource(skjermingDto)).thenReturn(skjermingResource)

        val klasseDto: KlasseDto = mock()
        val klasseResource: KlasseResource = mock()
        whenever(klasseMappingService.toKlasse(listOf(klasseDto))).thenReturn(listOf(klasseResource))

        val partDto: PartDto = mock()
        val partResource: PartResource = mock()
        whenever(partMappingService.toPartResource(partDto)).thenReturn(partResource)

        val nySakDto =
            SakDto
                .builder()
                .tittel("testTittel")
                .offentligTittel("testOffentligTittel")
                .saksmappetype("testSaksmappetype")
                .saksstatus("testSaksstatus")
                .tilgangsgruppe("testTilgangsgruppe")
                .administrativEnhet("testAdministrativEnhet")
                .saksansvarlig("testSaksansvarlig")
                .arkivdel("testArkivdel")
                .skjerming(skjermingDto)
                .klasse(listOf(klasseDto))
                .part(listOf(partDto))
                .build()

        val sakResource = sakMappingService.toSakResource(nySakDto)

        assertThat(sakResource.tittel).isEqualTo("testTittel")
        assertThat(sakResource.offentligTittel).isEqualTo("testOffentligTittel")
        assertThat(getLinkURL(sakResource.links, "saksmappetype")).isEqualTo("testSaksmappetype")
        assertThat(getLinkURL(sakResource.links, "saksstatus")).isEqualTo("testSaksstatus")
        assertThat(getLinkURL(sakResource.links, "tilgangsgruppe")).isEqualTo("testTilgangsgruppe")
        assertThat(getLinkURL(sakResource.links, "administrativEnhet")).isEqualTo("testAdministrativEnhet")
        assertThat(getLinkURL(sakResource.links, "saksansvarlig")).isEqualTo("testSaksansvarlig")
        assertThat(getLinkURL(sakResource.links, "arkivdel")).isEqualTo("testArkivdel")
        assertThat(sakResource.skjerming).isEqualTo(skjermingResource)
        assertThat(sakResource.klasse).containsOnly(klasseResource)
        assertThat(sakResource.part).containsOnly(partResource)

        verify(skjermingMappingService, times(1)).toSkjermingResource(skjermingDto)
        verifyNoMoreInteractions(skjermingMappingService)
        verify(klasseMappingService, times(1)).toKlasse(listOf(klasseDto))
        verifyNoMoreInteractions(klasseMappingService)
        verify(partMappingService, times(1)).toPartResource(partDto)
        verifyNoMoreInteractions(partMappingService)
    }

    @Test
    fun testToSakResourceWithNullDtoReturnsNull() {
        assertThatThrownBy { sakMappingService.toSakResource(null) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("sakDto cannot be null")
    }

    private fun getLinkURL(
        links: Map<String, List<Link>>,
        relation: String,
    ): String? = links[relation]?.firstOrNull()?.href
}
