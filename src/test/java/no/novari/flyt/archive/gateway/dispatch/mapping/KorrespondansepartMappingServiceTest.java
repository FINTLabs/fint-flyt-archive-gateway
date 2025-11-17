package no.novari.flyt.archive.gateway.dispatch.mapping;

import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.novari.flyt.archive.gateway.dispatch.model.instance.AdresseDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.KontaktinformasjonDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.KorrespondansepartDto;
import no.novari.flyt.archive.gateway.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KorrespondansepartMappingServiceTest {

    private KorrespondansepartMappingService korrespondansepartMappingService;
    private AdresseMappingService adresseMappingService;
    private KontaktinformasjonMappingService kontaktinformasjonMappingService;
    private SkjermingMappingService skjermingMappingService;

    @BeforeEach
    public void setup() {
        adresseMappingService = mock(AdresseMappingService.class);
        kontaktinformasjonMappingService = mock(KontaktinformasjonMappingService.class);
        skjermingMappingService = mock(SkjermingMappingService.class);
        korrespondansepartMappingService = new KorrespondansepartMappingService(adresseMappingService, kontaktinformasjonMappingService, skjermingMappingService);
    }

    @Test
    public void testToKorrespondansepartResource() {
        KorrespondansepartDto korrespondansepartDto = KorrespondansepartDto.builder()
                .korrespondanseparttype("type")
                .fodselsnummer("123456789")
                .organisasjonsnummer("987654321")
                .korrespondansepartNavn("name")
                .kontaktperson("contactPerson")
                .adresse(AdresseDto.builder().build())
                .kontaktinformasjon(KontaktinformasjonDto.builder().build())
                .skjerming(SkjermingDto.builder().build())
                .build();

        List<KorrespondansepartDto> dtoList = Collections.singletonList(korrespondansepartDto);

        List<KorrespondansepartResource> korrespondansepartResources = korrespondansepartMappingService.toKorrespondansepartResource(dtoList);

        assertThat(korrespondansepartResources).isNotNull();
        assertThat(korrespondansepartResources.size()).isEqualTo(1);

        assertThat(korrespondansepartDto.getFodselsnummer()).isPresent();
        assertThat(korrespondansepartDto.getOrganisasjonsnummer()).isPresent();
        assertThat(korrespondansepartDto.getKorrespondansepartNavn()).isPresent();
        assertThat(korrespondansepartDto.getKontaktperson()).isPresent();
        assertThat(korrespondansepartDto.getAdresse()).isPresent();
        assertThat(korrespondansepartDto.getKontaktinformasjon()).isPresent();
        assertThat(korrespondansepartDto.getSkjerming()).isPresent();

        KorrespondansepartResource korrespondansepartResource = korrespondansepartResources.getFirst();
        assertThat(korrespondansepartDto.getFodselsnummer().get())
                .isEqualTo(korrespondansepartResource.getFodselsnummer());
        assertThat(korrespondansepartDto.getOrganisasjonsnummer().get())
                .isEqualTo(korrespondansepartResource.getOrganisasjonsnummer());
        assertThat(korrespondansepartDto.getKorrespondansepartNavn().get())
                .isEqualTo(korrespondansepartResource.getKorrespondansepartNavn());
        assertThat(korrespondansepartDto.getKontaktperson().get())
                .isEqualTo(korrespondansepartResource.getKontaktperson());

        verify(adresseMappingService).toAdresseResource(korrespondansepartDto.getAdresse().get());
        verify(kontaktinformasjonMappingService).toKontaktinformasjon(korrespondansepartDto.getKontaktinformasjon().get());
        verify(skjermingMappingService).toSkjermingResource(korrespondansepartDto.getSkjerming().get());
    }
}
