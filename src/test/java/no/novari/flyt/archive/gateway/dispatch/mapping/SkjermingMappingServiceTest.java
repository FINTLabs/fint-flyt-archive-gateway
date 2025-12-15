package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkjermingMappingServiceTest {

    private SkjermingMappingService skjermingMappingService;

    @BeforeEach
    void setUp() {
        skjermingMappingService = new SkjermingMappingService();
    }

    @Test
    void toSkjermingResource_NullDto_ReturnsNull() {
        SkjermingDto skjermingDto = null;
        SkjermingResource result = skjermingMappingService.toSkjermingResource(skjermingDto);

        assertThat(result).isNull();
    }

    @Test
    void toSkjermingResource_ValidDto_ReturnsResource() {
        SkjermingDto skjermingDto = SkjermingDto.builder()
                .tilgangsrestriksjon("Tilgangsrestriksjon")
                .skjermingshjemmel("Skjermingshjemmel")
                .build();

        SkjermingResource result = skjermingMappingService.toSkjermingResource(skjermingDto);

        assertThat(result).isNotNull();
        assertThat(result.getTilgangsrestriksjon()).isNotEmpty();
        assertThat(result.getTilgangsrestriksjon().getFirst().getHref()).isEqualTo("Tilgangsrestriksjon");
        assertThat(result.getSkjermingshjemmel()).isNotEmpty();
        assertThat(result.getSkjermingshjemmel().getFirst().getHref()).isEqualTo("Skjermingshjemmel");
    }
}
