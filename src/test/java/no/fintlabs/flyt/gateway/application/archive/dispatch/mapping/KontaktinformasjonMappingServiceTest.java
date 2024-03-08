package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.KontaktinformasjonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KontaktinformasjonMappingServiceTest {

    private KontaktinformasjonMappingService kontaktinformasjonMappingService;

    @BeforeEach
    public void setup() {
        kontaktinformasjonMappingService = new KontaktinformasjonMappingService();
    }

    @Test
    public void testToKontaktinformasjonWithNullInput() {
        assertNull(kontaktinformasjonMappingService.toKontaktinformasjon(null));
    }

    @Test
    public void testToKontaktinformasjonWithValidInput() {
        KontaktinformasjonDto dto = KontaktinformasjonDto.builder()
                .epostadresse("testEmail@test.com")
                .telefonnummer("123456789")
                .mobiltelefonnummer("987654321")
                .build();

        Kontaktinformasjon actualKontaktinformasjon = kontaktinformasjonMappingService.toKontaktinformasjon(dto);

        assertEquals(dto.getEpostadresse().get(), actualKontaktinformasjon.getEpostadresse());
        assertEquals(dto.getTelefonnummer().get(), actualKontaktinformasjon.getTelefonnummer());
        assertEquals(dto.getMobiltelefonnummer().get(), actualKontaktinformasjon.getMobiltelefonnummer());
    }
}
