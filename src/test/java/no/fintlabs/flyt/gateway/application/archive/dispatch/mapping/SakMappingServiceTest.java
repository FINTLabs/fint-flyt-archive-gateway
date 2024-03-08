package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.PartResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.PartDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SakDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    // TODO: 18/08/2023 add part + more assertions
    @Test
    void shouldMapToSakResource() {
        when(skjermingMappingService.toSkjermingResource(any(SkjermingDto.class))).thenReturn(new SkjermingResource());
        when(klasseMappingService.toKlasse(anyList())).thenReturn(List.of());
        when(partMappingService.toPartResource(any(PartDto.class))).thenReturn(new PartResource());

        SakDto nySakDto = SakDto
                .builder()
                .tittel("testTittel")
                .offentligTittel("testOffentligTittel")
                .saksmappetype("testSaksmappetype")
                .saksstatus("testSaksstatus")
                .journalenhet("testJournalenhet")
                .administrativEnhet("testAdministrativEnhet")
                .saksansvarlig("testSaksansvarlig")
                .arkivdel("testArkivdel")
//                .part(
//                        List.of(
//                                PartDto.builder()
//                                        .partNavn("testNavn")
//                                        .partRolle("testRolle")
//                                        .kontaktperson("testPerson")
//                                        .adresse(AdresseDto.builder().adresselinje(new ArrayList<>()).postnummer("1234").poststed("testSted").build())
//                                        .kontaktinformasjon(KontaktinformasjonDto.builder().epostadresse("test@test.com").telefonnummer("1234567890").mobiltelefonnummer("0987654321").build())
//                                        .build()
//                        )
//                )
                .build();


        SakResource sakResource = sakMappingService.toSakResource(nySakDto);


        assertEquals("testTittel", sakResource.getTittel());
        assertEquals("testOffentligTittel", sakResource.getOffentligTittel());

        Map<String, List<Link>> resourceLinks = sakResource.getLinks();

        assertEquals("testSaksmappetype", getLinkURL(resourceLinks, "saksmappetype"));
        assertEquals("testSaksstatus", getLinkURL(resourceLinks, "saksstatus"));
        assertEquals("testJournalenhet", getLinkURL(resourceLinks, "journalenhet"));
        assertEquals("testAdministrativEnhet", getLinkURL(resourceLinks, "administrativEnhet"));
        assertEquals("testSaksansvarlig", getLinkURL(resourceLinks, "saksansvarlig"));
        assertEquals("testArkivdel", getLinkURL(resourceLinks, "arkivdel"));
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
