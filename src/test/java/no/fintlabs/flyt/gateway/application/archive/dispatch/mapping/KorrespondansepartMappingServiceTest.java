package no.fintlabs.flyt.gateway.application.archive.dispatch.mapping;

import no.fint.model.resource.arkiv.noark.KorrespondansepartResource;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.AdresseDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.KontaktinformasjonDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.KorrespondansepartDto;
import no.fintlabs.flyt.gateway.application.archive.dispatch.model.instance.SkjermingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

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

        assertNotNull(korrespondansepartResources);
        assertEquals(1, korrespondansepartResources.size());

        KorrespondansepartResource korrespondansepartResource = korrespondansepartResources.get(0);
        assertEquals(korrespondansepartDto.getFodselsnummer().get(), korrespondansepartResource.getFodselsnummer());
        assertEquals(korrespondansepartDto.getOrganisasjonsnummer().get(), korrespondansepartResource.getOrganisasjonsnummer());
        assertEquals(korrespondansepartDto.getKorrespondansepartNavn().get(), korrespondansepartResource.getKorrespondansepartNavn());
        assertEquals(korrespondansepartDto.getKontaktperson().get(), korrespondansepartResource.getKontaktperson());

        verify(adresseMappingService, times(1)).toAdresseResource(korrespondansepartDto.getAdresse().get());
        verify(kontaktinformasjonMappingService, times(1)).toKontaktinformasjon(korrespondansepartDto.getKontaktinformasjon().get());
        verify(skjermingMappingService, times(1)).toSkjermingResource(korrespondansepartDto.getSkjerming().get());
    }
}
