package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.KlasseDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class KlasseMappingServiceTest {

    private SkjermingMappingService mockSkjermingMappingService;
    private KlasseMappingService klasseMappingService;

    @BeforeEach
    public void setup() {
        mockSkjermingMappingService = Mockito.mock(SkjermingMappingService.class);
        klasseMappingService = new KlasseMappingService(mockSkjermingMappingService);
    }

    @Test
    public void testToKlasseWithNullInput() {
        assertNull(klasseMappingService.toKlasse(null));
    }

    @Test
    public void testToKlasseWithValidInput() {
        SkjermingDto skjermingDto = SkjermingDto.builder()
                .tilgangsrestriksjon("testTilgangsrestriksjon")
                .skjermingshjemmel("testSkjermingshjemmel")
                .build();

        KlasseDto dto = KlasseDto.builder()
                .klasseId("testId")
                .skjerming(skjermingDto)
                .tittel("testTitle")
                .klassifikasjonssystem("testKlassifikasjonssystem")
                .rekkefolge(1)
                .build();

        KlasseResource expectedResource = new KlasseResource();
        expectedResource.setKlasseId("testId");
        when(mockSkjermingMappingService.toSkjermingResource(any(SkjermingDto.class)))
                .thenReturn(new SkjermingResource());
        expectedResource.setSkjerming(new SkjermingResource());
        expectedResource.setTittel("testTitle");
        expectedResource.addKlassifikasjonssystem(Link.with("testKlassifikasjonssystem"));
        expectedResource.setRekkefolge(1);

        List<KlasseDto> klasseDtos = List.of(dto);
        List<KlasseResource> klasseResources = klasseMappingService.toKlasse(klasseDtos);

        assertNotNull(klasseResources);
        assertEquals(1, klasseResources.size());
        assertEquals(expectedResource, klasseResources.get(0));
    }

}
