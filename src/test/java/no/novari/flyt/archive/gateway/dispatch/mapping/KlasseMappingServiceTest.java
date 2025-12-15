package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.KlasseDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        List<KlasseResource> klasser = klasseMappingService.toKlasse(null);

        assertThat(klasser).isNull();
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

        when(mockSkjermingMappingService.toSkjermingResource(any(SkjermingDto.class)))
                .thenReturn(new SkjermingResource());

        KlasseResource expectedResource = new KlasseResource();
        expectedResource.setKlasseId("testId");

        expectedResource.setSkjerming(new SkjermingResource());
        expectedResource.setTittel("testTitle");
        expectedResource.addKlassifikasjonssystem(Link.with("testKlassifikasjonssystem"));
        expectedResource.setRekkefolge(1);

        List<KlasseDto> klasseDtos = List.of(dto);
        List<KlasseResource> klasseResources = klasseMappingService.toKlasse(klasseDtos);

        assertThat(klasseResources).isNotNull();
        assertThat(klasseResources.size()).isEqualTo(1);
        assertThat(klasseResources.getFirst()).isEqualTo(expectedResource);
    }

}
