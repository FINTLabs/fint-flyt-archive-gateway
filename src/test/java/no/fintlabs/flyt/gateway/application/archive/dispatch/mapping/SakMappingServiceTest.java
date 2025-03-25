package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.KlasseDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.PartDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SakDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SakMappingServiceTest {

    private SkjermingMappingService skjermingMappingService;
    private KlasseMappingService klasseMappingService;
    private SakMappingService sakMappingService;
    private PartMappingService partMappingService;

    @BeforeEach
    void setup() {
        skjermingMappingService = mock(SkjermingMappingService.class);
        klasseMappingService = mock(KlasseMappingService.class);
        partMappingService = mock(PartMappingService.class);
        sakMappingService = new SakMappingService(skjermingMappingService, klasseMappingService, partMappingService);
    }

    @Test
    void shouldMapToSakResource() {
        SkjermingDto skjermingDto = mock(SkjermingDto.class);
        SkjermingResource skjermingResource = mock(SkjermingResource.class);
        when(skjermingMappingService.toSkjermingResource(skjermingDto)).thenReturn(skjermingResource);

        KlasseDto klasseDto = mock(KlasseDto.class);
        KlasseResource klasseResource = mock(KlasseResource.class);
        when(klasseMappingService.toKlasse(List.of(klasseDto))).thenReturn(List.of(klasseResource));

        PartDto partDto = mock(PartDto.class);
        PartResource partResource = mock(PartResource.class);
        when(partMappingService.toPartResource(partDto)).thenReturn(partResource);

        SakDto nySakDto = SakDto
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
                .klasse(List.of(klasseDto))
                .part(List.of(partDto))
                .build();

        SakResource sakResource = sakMappingService.toSakResource(nySakDto);

        assertEquals("testTittel", sakResource.getTittel());
        assertEquals("testOffentligTittel", sakResource.getOffentligTittel());

        Map<String, List<Link>> resourceLinks = sakResource.getLinks();

        assertEquals("testSaksmappetype", getLinkURL(resourceLinks, "saksmappetype"));
        assertEquals("testSaksstatus", getLinkURL(resourceLinks, "saksstatus"));
        assertEquals("testTilgangsgruppe", getLinkURL(resourceLinks, "tilgangsgruppe"));
        assertEquals("testAdministrativEnhet", getLinkURL(resourceLinks, "administrativEnhet"));
        assertEquals("testSaksansvarlig", getLinkURL(resourceLinks, "saksansvarlig"));
        assertEquals("testArkivdel", getLinkURL(resourceLinks, "arkivdel"));
        assertThat(sakResource.getSkjerming()).isEqualTo(skjermingResource);
        assertThat(sakResource.getKlasse()).containsOnly(klasseResource);
        assertThat(sakResource.getPart()).containsOnly(partResource);

        verify(skjermingMappingService, times(1)).toSkjermingResource(skjermingDto);
        verifyNoMoreInteractions(skjermingMappingService);

        verify(klasseMappingService, times(1)).toKlasse(List.of(klasseDto));
        verifyNoMoreInteractions(klasseMappingService);

        verify(partMappingService, times(1)).toPartResource(partDto);
        verifyNoMoreInteractions(partMappingService);
    }

    @Test
    void testToSakResourceWithNullDtoReturnsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sakMappingService.toSakResource(null)
        );

        assertEquals("sakDto cannot be null", exception.getMessage());
    }


    private String getLinkURL(Map<String, List<Link>> links, String relation) {
        List<Link> linkList = links.get(relation);
        if (linkList != null && !linkList.isEmpty()) {
            return linkList.get(0).getHref();
        }
        return null;
    }
}
