package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.resource.arkiv.noark.SkjermingResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNull(result);
    }

    @Test
    void toSkjermingResource_ValidDto_ReturnsResource() {
        SkjermingDto skjermingDto = SkjermingDto.builder()
                .tilgangsrestriksjon("Tilgangsrestriksjon")
                .skjermingshjemmel("Skjermingshjemmel")
                .build();

        SkjermingResource result = skjermingMappingService.toSkjermingResource(skjermingDto);

        assertNotNull(result);
        assertFalse(result.getTilgangsrestriksjon().isEmpty());
        assertEquals("Tilgangsrestriksjon", result.getTilgangsrestriksjon().get(0).getHref());
        assertFalse(result.getSkjermingshjemmel().isEmpty());
        assertEquals("Skjermingshjemmel", result.getSkjermingshjemmel().get(0).getHref());
    }
}
