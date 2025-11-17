package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.novari.flyt.archive.gateway.dispatch.model.instance.KontaktinformasjonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KontaktinformasjonMappingServiceTest {

    private KontaktinformasjonMappingService kontaktinformasjonMappingService;

    @BeforeEach
    public void setup() {
        kontaktinformasjonMappingService = new KontaktinformasjonMappingService();
    }

    @Test
    public void testToKontaktinformasjonWithNullInput() {
        Kontaktinformasjon kontaktinformasjon = kontaktinformasjonMappingService.toKontaktinformasjon(null);

        assertThat(kontaktinformasjon).isNull();
    }

    @Test
    public void testToKontaktinformasjonWithValidInput() {
        KontaktinformasjonDto dto = KontaktinformasjonDto.builder()
                .epostadresse("testEmail@test.com")
                .telefonnummer("123456789")
                .mobiltelefonnummer("987654321")
                .build();

        Kontaktinformasjon actualKontaktinformasjon = kontaktinformasjonMappingService.toKontaktinformasjon(dto);

        assertThat(dto.getEpostadresse()).isPresent();
        assertThat(dto.getEpostadresse().get()).isEqualTo(actualKontaktinformasjon.getEpostadresse());
        assertThat(dto.getTelefonnummer()).isPresent();
        assertThat(dto.getTelefonnummer().get()).isEqualTo(actualKontaktinformasjon.getTelefonnummer());
        assertThat(dto.getMobiltelefonnummer()).isPresent();
        assertThat(dto.getMobiltelefonnummer().get()).isEqualTo(actualKontaktinformasjon.getMobiltelefonnummer());
    }
}
